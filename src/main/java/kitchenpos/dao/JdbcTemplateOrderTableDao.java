package kitchenpos.dao;

import kitchenpos.domain.OrderTable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JdbcTemplateOrderTableDao implements OrderTableDao {
    private static final String TABLE_NAME = "order_table";
    private static final String KEY_COLUMN_NAME = "id";
    private static final RowMapper<OrderTable> ORDER_TABLE_ROW_MAPPER = (rs, rowNum) -> new OrderTable(
            rs.getLong(KEY_COLUMN_NAME),
            rs.getObject("table_group_id", Long.class),
            rs.getInt("number_of_guests"),
            rs.getBoolean("empty")
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcTemplateOrderTableDao(final DataSource dataSource) {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns(KEY_COLUMN_NAME)
        ;
    }

    public OrderTable save(final OrderTable entity) {
        if (Objects.isNull(entity.getId())) {
            final SqlParameterSource parameters = new BeanPropertySqlParameterSource(entity);
            final Number key = jdbcInsert.executeAndReturnKey(parameters);
            return select(key.longValue());
        }
        update(entity);
        return entity;
    }

    public Optional<OrderTable> findById(final Long id) {
        try {
            return Optional.of(select(id));
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<OrderTable> findAll() {
        final String sql = "SELECT id, table_group_id, number_of_guests, empty FROM order_table";
        return jdbcTemplate.query(sql, ORDER_TABLE_ROW_MAPPER);
    }

    public List<OrderTable> findAllByIdIn(final List<Long> ids) {
        final String sql = "SELECT id, table_group_id, number_of_guests, empty FROM order_table WHERE id IN (:ids)";
        final SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("ids", ids);
        return jdbcTemplate.query(sql, parameters, ORDER_TABLE_ROW_MAPPER);
    }

    public List<OrderTable> findAllByTableGroupId(final Long tableGroupId) {
        final String sql = "SELECT id, table_group_id, number_of_guests, empty" +
                " FROM order_table WHERE table_group_id = (:tableGroupId)";
        final SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("tableGroupId", tableGroupId);
        return jdbcTemplate.query(sql, parameters, ORDER_TABLE_ROW_MAPPER);
    }

    private OrderTable select(final Long id) {
        final String sql = "SELECT id, table_group_id, number_of_guests, empty FROM order_table WHERE id = (:id)";
        final SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", id);
        return jdbcTemplate.queryForObject(sql, parameters, ORDER_TABLE_ROW_MAPPER);
    }

    private void update(final OrderTable entity) {
        final String sql = "UPDATE order_table SET table_group_id = (:tableGroupId)," +
                " number_of_guests = (:numberOfGuests), empty = (:empty) WHERE id = (:id)";
        final SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("tableGroupId", entity.getTableGroupId())
                .addValue("numberOfGuests", entity.getNumberOfGuests())
                .addValue("empty", entity.isEmpty())
                .addValue("id", entity.getId());
        jdbcTemplate.update(sql, parameters);
    }
}
