package test.com.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import test.com.model.Main;

@Repository
public interface MainDao extends JpaRepository<Main, Long> {
	@Query("SELECT s FROM Main m WHERE m.shopId = :shopId and m.inputDate = :inputDate")
	public Main findByShopIdShopName(@Param("shopId") String shopId,
			@Param("inputDate") Date inputDate);
	
	@Query("SELECT m FROM Main m WHERE m.inputDate = :inputDate")
	 public List<Main> findByinputDate(@Param("inputDate") Date inputDate);

}
