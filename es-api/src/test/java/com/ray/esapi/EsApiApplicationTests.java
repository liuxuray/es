package com.ray.esapi;

import com.alibaba.fastjson.JSON;
import com.ray.esapi.domain.User;
import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;

@SpringBootTest
class EsApiApplicationTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    @Test
    void createIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("llxx");
        CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    @Test
    void getIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("llxx");
        System.out.println(restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT));
    }

    @Test
    void delIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("llxx");
        AcknowledgedResponse response = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
    }


    //================================================================
    @Test
    void createUser() throws IOException {
        User user = new User("??????", 78);
        //?????????????????????????????????
        IndexRequest indexRequest = new IndexRequest("llxx");

        // PUT llxx/_doc/id
        indexRequest.id("1");
        indexRequest.timeout(TimeValue.timeValueSeconds(1));

        //??????????????????
        indexRequest.source(JSON.toJSONString(user), XContentType.JSON);
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(indexResponse.toString());
        System.out.println(indexResponse.status());
    }

    @Test
    void getUser() throws IOException {
        GetRequest getRequest = new GetRequest("llxx", "1");
        GetResponse response = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(response.getSourceAsString());
        System.out.println(response);
    }


    @Test
    void isExist() throws IOException {
        GetRequest getRequest = new GetRequest("llxx", "1");
        //?????????????????????????????????????????????????????? _source ?????????
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        System.out.println(restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT));
    }

        @Test
        void updateUser() throws IOException {
            UpdateRequest updateRequest = new UpdateRequest("llxx", "1");
            updateRequest.timeout("1s");
            User user = new User("??????", 45);
            //???????????????????????????doc???
            updateRequest.doc(JSON.toJSONString(user), XContentType.JSON);
            UpdateResponse response = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            System.out.println(response.status());
        }

    @Test
    void delUser() throws IOException {
        DeleteRequest deleteRequest = new DeleteRequest("llxx", "1");
        deleteRequest.timeout("1s");
        DeleteResponse response = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        System.out.println(response.status());
    }

    @Test
    void testBulkRequest() throws IOException {
        //1.????????????????????????
        BulkRequest bulkRequest = new BulkRequest();
        //??????????????????????????????
        bulkRequest.timeout("10s");
        //2.??????????????????
        ArrayList<User> userList = new ArrayList<>();
        userList.add(new User("maybe", 21));
        userList.add(new User("??????", 22));
        userList.add(new User("??????", 20));
        userList.add(new User("??????", 23));
        //3.?????????????????????
        for (int i = 0; i < userList.size(); i++) {
            //????????????????????????????????????????????????????????????????????????
            bulkRequest.add(
                    new IndexRequest("llxx")
                            //??????id?????????????????????id
                            .id("" + (i + 1))
                            .source(JSON.toJSONString(userList.get(i)), XContentType.JSON)
            );
        }
        //4.????????????
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        //5.?????? ????????????????????????
        RestStatus status = bulkResponse.status();
        System.out.println(status.getStatus());
    }

    @Test
    void testSearch() throws IOException {
        SearchRequest searchRequest = new SearchRequest("llxx");

        //??????????????????
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //query builder
        //QueryBuilders.bool()
        //QueryBuilders.match()
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "maybe");
        sourceBuilder.query(termQueryBuilder);

        //??????
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name");

        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //????????????????????? Hits???
        System.out.println(JSON.toJSONString(searchResponse.getHits()));
    }




    @Test
    void contextLoads() {
    }


}
