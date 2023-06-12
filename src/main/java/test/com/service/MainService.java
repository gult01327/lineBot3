package test.com.service;

import org.springframework.data.jpa.repository.JpaRepository;

import test.com.vo.MainVo;

public interface MainService extends JpaRepository<MainVo, Long>{
}
