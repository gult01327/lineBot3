package test.com.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import test.com.model.Detail;

@Repository
public interface DetailDao extends JpaRepository<Detail, Long>{
	
	@Modifying
	@Query("update detail d SET d.drink = :newdrink ,d.sugar = :sugar ,d.ice = :ice ,d.size = :size ,"
			+ "d.price = :price , d.update = :update WHERE d.drink = :oldDrink and  d.user_name = :userName and  d.drink = :oldDrink")
	public Detail update( @Param("oldDrink") String oldDrink,@Param("newdrink") String newdrink, @Param("sugar") String sugar,
			@Param("ice") String ice,@Param("size") String size,@Param("price") int price,@Param("update") Date update,
			@Param("userName") String userName);
}
