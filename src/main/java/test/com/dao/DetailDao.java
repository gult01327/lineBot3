package test.com.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import test.com.model.Detail;

@Repository
public interface DetailDao extends JpaRepository<Detail, Long>{
	

}
