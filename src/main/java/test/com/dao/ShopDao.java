package test.com.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import test.com.model.Shop;

@Repository
public interface ShopDao extends JpaRepository<Shop, Long>{

}
