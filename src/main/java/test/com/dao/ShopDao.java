package test.com.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import test.com.model.Shop;

@Repository
public interface ShopDao extends JpaRepository<Shop, Long>{
	@Query("SELECT s FROM Shop s WHERE s.shopId = :shopId and s.inputDate = :inputDate")
	 public Shop findByShopIdInputDate(@Param("shopId") String shopId,@Param("inputDate") Date inputDate);
	
	@Query("SELECT s FROM Shop s WHERE s.inputDate = :inputDate")
	 public List<Shop> findByinputDate(@Param("inputDate") Date inputDate);
	
	@Query("SELECT s FROM Shop s WHERE s.inputDate = :inputDate and s.orderStatus='1' ")
	 public Shop findByStatusInputDate(@Param("inputDate") Date inputDate);
	
}
