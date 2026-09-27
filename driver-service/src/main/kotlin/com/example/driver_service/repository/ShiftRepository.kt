package com.example.driver_service.repository

import com.example.driver_service.model.entity.ShiftEntity
import java.util.UUID
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update

@Mapper
interface ShiftRepository {
    @Insert(
        """
        INSERT INTO shift_table (id, driver_id, car_id, start_at)
        VALUES (#{id}, #{driverId}, #{carId}, #{startAt})
        """
    )
    fun save(shift: ShiftEntity)

    @Update(
        """
        UPDATE shift_table SET 
          driver_id = #{driverId},  car_id = #{carId}, status = #{status}, start_at = #{startAt}, end_at = #{endAt}, 
            total_rides = #{totalRides},total_earnings = #{totalEarnings}, total_distance_meters = #{totalDistanceMeters}
        WHERE id = #{id}
    """
    )
    fun update(shift: ShiftEntity)

    @Select("""SELECT * FROM shift_table WHERE driver_id=#{driverId} AND status='OPEN'""")
    fun findOpened(driverId: UUID): ShiftEntity?

    @Select("""SELECT * FROM shift_table WHERE id=#{id}""")
    fun findById(id: UUID): ShiftEntity?

    @Update("TRUNCATE TABLE shift_table RESTART IDENTITY CASCADE")
    fun deleteAll()

}