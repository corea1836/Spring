## 스프링부트 Rest Api CRUD



### - Create, Update



**1. Controller**

````java
// FaqController

@RestController
public class FAQController {
  // Faq 게시판 컨트롤러

    @Autowired
    FAQService faqService;
  // 복잡한 로직을 수행할 서비스 인터페이스 DI

    @RequestMapping(
      							value = {"/faq/{groupNo}", "/faq/{groupNo}/{faqNo}"}, 
                    method = {RequestMethod.POST, RequestMethod.PUT}
    )
  // @RequestMapping에서 다중 매핑을 하는 법 1. URL을 판별하여 분기 처리, 2. @PathVariable을 사용
    public ResponseEntity<JSONObject> registerFaq (@PathVariable(value = "faqNo", required = false) Long faqNo,
                                                   @PathVariable(value = "groupNo") int groupNo,
                                  								 @RequestBody FAQDto faqDto){
      // faqNo는 C, U를 분기하는 파라미터로 설정(required = false) => 있으면 U, 없으면 C
      // groupNo는 int 값으로 설정
        JSONObject jsonObj = new JSONObject();
      // 모든 로직을 처리하고 결과를 담을 JSONObject
        faqDto.setGroupNo(groupNo);
      // 해당 공동구에에 대한 FAQ이므로 전달받은 groupNo를 faqDto에 set
        try {
            if (faqNo != null) {
                faqDto.setFaqNo(faqNo.intValue());
              // int는 null 체킹이 되지 않는다. 전체 DB는 int로 설정되어 있기 때문에 참조 오류가 발생할 수도 있었다.
              // 때문에 faqNo는 null 체킹용으로 Long으로 받고 체크 후 int로 바꿔서 faqDto에 set
            }
            boolean isRegistered = faqService.registerFaq(faqDto);
          // C인지 U인지 판단할 수 있는 명확한 기준이 생겼으므로(DTO) 서비스 로직 수행
        } catch (DataAccessException e) {
            jsonObj.append("result", "데이터베이스 처리과정 중 문제가 발생하였습니다. 입력값을 다시 확인해주세요.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonObj);
        } catch (Exception e) {
            jsonObj.append("message", "시스템에 문제가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonObj);
        }
        return ResponseEntity.status(HttpStatus.OK).body(jsonObj);
    }
  ...
}
````



**2.Service**

```java
// FaqService

public interface FAQService {
  // 서비스 인터페이스

    boolean registerFaq(FAQDto faqDto);

    List<FAQDto> readFaq(int groupNo, int categoryNo);
    
    boolean deleteFaq(int groupNo, int faqNo);
}

// FaqServiceImpl

@Service
public class FAQServiceImpl implements FAQService{
// 서비스 인터페이스 구현체
    @Autowired
    FAQDao faqDao;
  // Mapping을 위함 Dao = Mapper
    @Transactional
  // DB에 관여하는 서비스이므로 트랜잭션 처리
    @Override
    public boolean registerFaq(FAQDto faqDto) {
        int queryResult = 0;
      // 결과값을 담기위한 int 객체
        if (faqDto.getFaqNo() != 0){
            queryResult = faqDao.updateFaq(faqDto);
          // faqDto의 faqNo에 0이 아닌 값이 있다 = DB에 이미 있을 수 있다. 해당 객체를 Update하는 Mapping을 수행 하러 간다.
        } else {
            queryResult = faqDao.createFaq(faqDto);
          // faqNo가 0이라면 DB에 없는 자료일 수 있다. 해당 객체를 Create하는 Mapping을 수행하러 간다.
        }
        return (queryResult == 1) ? true : false;
      // 결과값이 정상적으로 작동한다면 SQL문을 수행한 row의 수를 반환한다. C, U는 1이 반환될것이므로 참아면 true, 아니면 false 반환
    }
}
```



**3. Mapper**

```java
// FAQDao

@Mapper
public interface FAQDao {
    public int createFaq(FAQDto faqDto);

    public int updateFaq(FAQDto faqDto);

    List<FAQDto> readFaq(int groupNo, int categoryNo);

    public FAQDto selectFaqDetail(int groupNo, int faqNo);

    int deleteFaq(int groupNo, int faqNo);
}

// faq_query.xml

    <insert id="createFaq" parameterType="com.dogather.pjtserver.dto.FAQDto">
        insert into faq
        (group_no, category_no, faq_question, faq_answer)
        values
        (#{groupNo}, #{categoryNo}, #{faqQuestion}, #{faqAnswer})
    </insert>
    
    <update id="updateFaq" parameterType="com.dogather.pjtserver.dto.FAQDto">
        update faq
        set
            faq_question = #{faqQuestion},
            faq_answer = #{faqAnswer}
        where
            faq_no = #{faqNo} and group_no = #{groupNo}
    </update>
```

