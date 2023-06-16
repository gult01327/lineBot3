package test.com.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import test.com.model.Shop;

@Repository
public interface ShopDao extends JpaRepository<Shop, Long>{
	@Query("SELECT s FROM Shop s WHERE s.shopId = :shopId and s.inputDate = :inputDate")
	 public Shop findByinputDate(@Param("shopId") String shopId,@Param("inputDate") Date inputDate);
}
