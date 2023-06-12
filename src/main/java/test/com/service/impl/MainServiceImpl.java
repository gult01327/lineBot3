//package test.com.service.impl;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.function.Function;
//
//import org.springframework.data.domain.Example;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
//import org.springframework.stereotype.Service;
//
//import test.com.service.MainService;
//import test.com.vo.MainVo;
//
//@Service
//public class MainServiceImpl implements MainService {
//
//	@Override
//	public List<MainVo> findAll() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<MainVo> findAll(Sort sort) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<MainVo> findAllById(Iterable<Long> ids) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <S extends MainVo> List<S> saveAll(Iterable<S> entities) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void flush() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public <S extends MainVo> S saveAndFlush(S entity) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <S extends MainVo> List<S> saveAllAndFlush(Iterable<S> entities) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void deleteAllInBatch(Iterable<MainVo> entities) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void deleteAllByIdInBatch(Iterable<Long> ids) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void deleteAllInBatch() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public MainVo getOne(Long id) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public MainVo getById(Long id) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public MainVo getReferenceById(Long id) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <S extends MainVo> List<S> findAll(Example<S> example) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <S extends MainVo> List<S> findAll(Example<S> example, Sort sort) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Page<MainVo> findAll(Pageable pageable) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <S extends MainVo> S save(S entity) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Optional<MainVo> findById(Long id) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public boolean existsById(Long id) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public long count() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public void deleteById(Long id) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void delete(MainVo entity) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void deleteAllById(Iterable<? extends Long> ids) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void deleteAll(Iterable<? extends MainVo> entities) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void deleteAll() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public <S extends MainVo> Optional<S> findOne(Example<S> example) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <S extends MainVo> Page<S> findAll(Example<S> example, Pageable pageable) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public <S extends MainVo> long count(Example<S> example) {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public <S extends MainVo> boolean exists(Example<S> example) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public <S extends MainVo, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
