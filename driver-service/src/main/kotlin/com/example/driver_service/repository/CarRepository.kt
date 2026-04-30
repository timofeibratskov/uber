package com.example.driver_service.repository

import com.example.driver_service.model.entity.CarEntity
import java.util.UUID
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update

@Mapper
interface CarRepository {
    @Select("SELECT * FROM car_table WHERE id = #{carId} AND driver_id = #{driverId}")
    fun findByCarIdAndDriverId(carId: UUID, driverId: UUID): CarEntity?

    @Select("SELECT * FROM car_table WHERE id = #{carId}")
    fun findById(carId: UUID): CarEntity?

    @Select("SELECT * FROM car_table WHERE driver_id = #{id} AND is_deleted = false")
    fun findByDriverId(id: UUID): List<CarEntity>

    @Select("SELECT * FROM car_table WHERE license_plate = #{plate}")
    fun findByLicensePlate(plate: String): CarEntity?

    @Insert(
        """
    INSERT INTO car_table (id, color, license_plate, brand, model, seats, driver_id, created_at, updated_at, is_deleted)
    VALUES (#{id}, #{color}, #{licensePlate}, #{brand}, #{model}, #{seats}, #{driverId}, #{createdAt}, #{updatedAt}, #{isDeleted})
    """
    )
    fun save(car: CarEntity)

    @Update(
        """
        UPDATE car_table SET 
            color = #{color}, brand = #{brand}, model = #{model}, 
            seats = #{seats}, is_deleted = #{isDeleted}, updated_at = NOW()
        WHERE id = #{id}
    """
    )
    fun update(car: CarEntity)

    @Update("UPDATE car_table SET is_deleted = true, updated_at = NOW() WHERE id = #{id}")
    fun softDeleteById(id: UUID): Int

    @Update("TRUNCATE TABLE car_table RESTART IDENTITY CASCADE")
    fun deleteAll()
}