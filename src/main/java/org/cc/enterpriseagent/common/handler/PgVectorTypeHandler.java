package org.cc.enterpriseagent.common.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** MyBatis handler for PostgreSQL pgvector values with 1024 dimensions. */
public class PgVectorTypeHandler extends BaseTypeHandler<List<Float>> {

    private static final int DIMENSIONS = 1024;

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Float> parameter,
                                    JdbcType jdbcType) throws SQLException {
        validate(parameter);

        PGobject vector = new PGobject();
        vector.setType("vector");
        vector.setValue(toVectorLiteral(parameter));
        ps.setObject(i, vector);
    }

    @Override
    public List<Float> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public List<Float> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public List<Float> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private String toVectorLiteral(List<Float> embedding) {
        return embedding.stream()
                .map(Object::toString)
                .collect(Collectors.joining(",", "[", "]"));
    }

    private List<Float> parse(String vector) throws SQLException {
        if (vector == null) {
            return null;
        }
        if (!vector.startsWith("[") || !vector.endsWith("]")) {
            throw new SQLException("无效的 pgvector 值: " + vector);
        }

        try {
            List<Float> embedding = Arrays.stream(vector.substring(1, vector.length() - 1).split(","))
                    .map(Float::valueOf)
                    .toList();
            validate(embedding);
            return embedding;
        } catch (NumberFormatException e) {
            throw new SQLException("pgvector 值解析失败", e);
        }
    }

    private void validate(List<Float> embedding) throws SQLException {
        if (embedding == null || embedding.size() != DIMENSIONS) {
            throw new SQLException("embedding 维度必须为 " + DIMENSIONS);
        }
        if (embedding.stream().anyMatch(value -> value == null || !Float.isFinite(value))) {
            throw new SQLException("embedding 不能包含 null、NaN 或 Infinity");
        }
    }
}
