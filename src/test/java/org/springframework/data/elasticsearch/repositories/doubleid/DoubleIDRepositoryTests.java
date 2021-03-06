/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.elasticsearch.repositories.doubleid;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.utils.IndexInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Mark Paluch
 * @author Christoph Strobl
 * @author Peter-Josef Meisch
 */
@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:/double-id-repository-test.xml")
public class DoubleIDRepositoryTests {

	@Autowired private DoubleIDRepository repository;

	@Autowired private ElasticsearchTemplate elasticsearchTemplate;

	@Before
	public void before() {
		IndexInitializer.init(elasticsearchTemplate, DoubleIDEntity.class);
	}

	@After
	public void after() {

		elasticsearchTemplate.deleteIndex(DoubleIDEntity.class);
	}

	@Test
	public void shouldDoBulkIndexDocument() {

		// given
		Double documentId1 = RandomUtils.nextDouble();
		DoubleIDEntity sampleEntity1 = new DoubleIDEntity();
		sampleEntity1.setId(documentId1);
		sampleEntity1.setMessage("some message");
		sampleEntity1.setVersion(System.currentTimeMillis());

		Double documentId2 = RandomUtils.nextDouble();
		DoubleIDEntity sampleEntity2 = new DoubleIDEntity();
		sampleEntity2.setId(documentId2);
		sampleEntity2.setMessage("some message");
		sampleEntity2.setVersion(System.currentTimeMillis());

		// when
		repository.saveAll(Arrays.asList(sampleEntity1, sampleEntity2));

		// then
		Optional<DoubleIDEntity> entity1FromElasticSearch = repository.findById(documentId1);
		assertThat(entity1FromElasticSearch).isPresent();

		Optional<DoubleIDEntity> entity2FromElasticSearch = repository.findById(documentId2);
		assertThat(entity2FromElasticSearch).isPresent();
	}

	@Test
	public void shouldSaveDocument() {

		// given
		Double documentId = RandomUtils.nextDouble();
		DoubleIDEntity sampleEntity = new DoubleIDEntity();
		sampleEntity.setId(documentId);
		sampleEntity.setMessage("some message");
		sampleEntity.setVersion(System.currentTimeMillis());

		// when
		repository.save(sampleEntity);

		// then
		Optional<DoubleIDEntity> entityFromElasticSearch = repository.findById(documentId);
		assertThat(entityFromElasticSearch).isPresent();
	}

	/**
	 * @author Rizwan Idrees
	 * @author Mohsin Husen
	 */

	@Document(indexName = "test-index-double-keyed-entity", type = "double-keyed-entity", shards = 1, replicas = 0,
			refreshInterval = "-1")
	static class DoubleIDEntity {

		@Id private Double id;
		private String type;
		private String message;
		@Version private Long version;

		public Double getId() {
			return id;
		}

		public void setId(Double id) {
			this.id = id;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public Long getVersion() {
			return version;
		}

		public void setVersion(Long version) {
			this.version = version;
		}
	}

	/**
	 * @author Ryan Henszey
	 * @author Mohsin Husen
	 */
	interface DoubleIDRepository extends ElasticsearchRepository<DoubleIDEntity, Double> {}
}
