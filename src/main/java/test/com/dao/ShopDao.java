package test.com.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import test.com.model.Shop;

@Repository
public interface ShopDao extends JpaRepository<Shop, Long>{
	 @Query("SELECT s FROM shop_order s WHERE s.shop_id = :shopId and s.input_date = :inputDate")
	 public Shop findByinputDate(@Param("shopId") String shopId,@Param("inputDate") String inputDate);
}
