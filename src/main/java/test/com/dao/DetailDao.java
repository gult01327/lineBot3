package test.com.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import test.com.model.Detail;

@Repository
public interface DetailDao extends JpaRepository<Detail, Long>{
	
	@Query("SELECT s FROM Detail s WHERE inputDate = :today and status='1'")
	 public List<Detail> findByinputDate(@Param("today") Date today);

}
