package lucene;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.apache.lucene.search.Query;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

@Repository
public class MySQLInputDAO implements InputDAO {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Override
	@Async
	@Transactional
	public Optional<Boolean> addInput(Input input) {
		
		{
			Session session = entityManager.unwrap(Session.class);
			
			SQLQuery sqlQuery = session.createSQLQuery("INSERT INTO input(prefix, next, pid, uid, frequency) VALUES ('"
					+input.getPrefix()+"', '"
					+input.getNext()+"', "
					+input.getPid()+", "
					+input.getUid()+", 1)"
					+ " ON DUPLICATE KEY UPDATE frequency=frequency+1;");
//			System.out.println(sqlQuery.getQueryString());
			int q = sqlQuery.executeUpdate();
//			System.out.println(q+ " Rows Updated");
			
		}
//		
//		{
//			Session session = entityManager.unwrap(Session.class);
//			FullTextSession fullTextSession = Search.getFullTextSession(session);
//			
//			Map<String, Object> propertyNameValues = new TreeMap<>();
//			propertyNameValues.put("prefix", input.getPrefix());
//			propertyNameValues.put("next", input.getNext());
//			propertyNameValues.put("pid", input.getPid());
//			propertyNameValues.put("uid", input.getUid());
//			
//			Criteria criteria = session.createCriteria(Input.class)
//									   .add(Restrictions.allEq(propertyNameValues));
//			
//			@SuppressWarnings("unchecked")
//			List<Input> list = criteria.list();
//			if(list.size() == 1){
//				input = list.get(0);
//				fullTextSession.index(input);
//			}
//			else
//				throw new RuntimeException();
//			
//		}
		
		return Optional.of(true);
	}

	@Override
	@Transactional
	public List<String> nextWords(Input input) {
		
		long currentTimeMillis = System.currentTimeMillis();
		
		FullTextEntityManager fullTextEntityManager = org.hibernate.search.jpa.Search.getFullTextEntityManager(entityManager);
		QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Input.class).get();
		
		Query query = queryBuilder.phrase()
								  .onField("prefix")
								  .sentence(input.getPrefix())
								  .createQuery();
		
		FullTextQuery jpaQuery = fullTextEntityManager .createFullTextQuery(query, Input.class);
		jpaQuery.setMaxResults(10);
		
		jpaQuery.setProjection("next");
		
//		List<Object[]> resultList = jpaQuery.getResultList();
//		Map<String, Object> resultMap = new HashMap<>(resultList.size());
//		for (Object[] result : resultList)
//		  resultMap.put((String)result[0], result[1]);
//		
//		System.out.println(resultMap);
//		
//		System.out.println();
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = jpaQuery.getResultList();
		List<String> collect = resultList.stream().map(objArr -> (String)objArr[0]).collect(Collectors.toList());
		System.out.println(System.currentTimeMillis() - currentTimeMillis);
		System.out.println(collect);
		System.out.println();
		
		return collect;
	}
	
}
