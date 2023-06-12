package test.com.service;

import org.springframework.data.jpa.repository.JpaRepository;

import test.com.vo.DetailVo;

public interface DetailService extends JpaRepository<DetailVo, Long>{

}
