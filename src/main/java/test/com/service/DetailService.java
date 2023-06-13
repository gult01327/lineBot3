package test.com.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import test.com.vo.Detail;

@Repository
public interface DetailService extends JpaRepository<Detail, Long>{

}
