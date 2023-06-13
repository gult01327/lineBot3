package test.com.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import test.com.vo.Main;

@Repository
public interface MainService extends JpaRepository<Main, Long>{
}
