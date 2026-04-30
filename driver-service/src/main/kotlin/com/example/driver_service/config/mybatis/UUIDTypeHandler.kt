package com.example.driver_service.config.mybatis

import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.UUID
import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import org.apache.ibatis.type.MappedTypes


@MappedTypes(UUID::class)
class UUIDTypeHandler : BaseTypeHandler<UUID>() {
    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: UUID, jdbcType: JdbcType?) {
        ps.setObject(i, parameter)
    }

    override fun setParameter(ps: PreparedStatement, i: Int, parameter: UUID?, jdbcType: JdbcType?) {
        if (parameter == null) {
            ps.setNull(i, java.sql.Types.OTHER)
        } else {
            super.setParameter(ps, i, parameter, jdbcType)
        }
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): UUID? =
        rs.getObject(columnName) as? UUID

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): UUID? =
        rs.getObject(columnIndex) as? UUID

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): UUID? =
        cs.getObject(columnIndex) as? UUID
}