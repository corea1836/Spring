## 스프링부트 Rest Api CRUD



### - Read



**1. Controller**

````java
// FaqController

@RestController
public class FAQController {
  // Faq 게시판 컨트롤러

    @Autowired
    FAQService faqService;
  // 복잡한 로직을 수행할 서비스 인터페이스 DI
  
  // Read
  
    @GetMapping(value={"/{groupNo}", "/{groupNo}/{categoryNo}"})
  // 두가지 로직 수행(특정 그룹의 전체 Faq조회, 특정 그룹의 특정 카테고리 Faq조회)
    public ResponseEntity<Map<String, Object>> readFaq(
      																@PathVariable(value = "categoryNo", required = false) Long categoryNo,
                                      @PathVariable(value = "groupNo") int groupNo){
      // 두가지 로직을 한 API에서 구현하기 위해 공통으로 필요한 groupNo는 필수값으로, 
      // categoryNo는 필수값을 false로 설정, Long으로 타입으로 설정해 null 체킹으로 로직을 분기
        Map<String, Object> result = new HashMap<>();
      // 결과값을 담을 HashMap 생성 value에는 해당값을 객체로 받음

        if (categoryNo != null) {
          // categoryNo가 null이 아니라면 => Faq의 특정 카테고리까지 조회를 해야 함
            List<FAQDto> readList = faqService.readFaq(groupNo, categoryNo.intValue());
          // 서비스로 파라미터를 넘길 때 categoryNo를 int로 바꿔서 넘김 => DB에 숫자로 된 값들이 모두 int라 Long으로 입력시 오류
            result.put("data amount", readList.size());
          // 조회된 faq의 사이즈를 data amount의 value로 저장
            result.put("data", readList);
          // 조회한 데이터를 data의 value로 저장
            return ResponseEntity.status(HttpStatus.OK).body(result);
          // 결과값 반환
        } else {
            List<FAQDto> readList = faqService.readFaqAll(groupNo);
          // categoryNo가 null 이라면 => 특정 그룹 내 전체 Faq 조회
            result.put("data amount", readList.size());
            result.put("data", readList);
            return ResponseEntity.status(HttpStatus.OK).body(result);
          // 위 로직과 동일
        }
  //===========================================================================================================
  
  // Delete
  
    @DeleteMapping("/faq/{groupNo}/{faqNo}")
  // 삭제 로직은 공동구매 그룹에 속한다면 faq
    public ResponseEntity<String> deleteFaq(@PathVariable("groupNo") int groupNo,
                                            @PathVariable("faqNo") int faqNo) {
        JSONObject jsonObj = new JSONObject();
      // 결과 메세지를 담을 JSONObject 생성
        try {
            boolean isDeleted = faqService.deleteFaq(groupNo, faqNo);
          // 비지니스 로직을 수행할 서비스 인터페이스에는 groupNo, faqNo를 파라미터로 갖고 boolean을 반환
            if (isDeleted) {
                jsonObj.append("result", "삭제 완료");
              // 수행한 비지니스 로직이 true라면 삭제 완료를 결과 메세지에 담음
            }
        } catch (DataAccessException e) {
            jsonObj.append("result", "데이터베이스 처리과정 중 문제가 발생하였습니다. 입력값을 다시 확인해주세요.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonObj.toString());
          // DB 로직 처리 중 오류가 발생한다면 DataAccessException 발생
        } catch (Exception e) {
            e.printStackTrace();
            jsonObj.append("message", "시스템에 문제가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonObj.toString());
          // 그 외의 오류는 시스템 에러로 메세지 반환
        }
        return ResponseEntity.status(HttpStatus.OK).body(jsonObj.toString());
      // 결과값 반환
    }
}
````



**2.Service**

```java
// FaqService

public interface FAQService {
  // 서비스 인터페이스
  ...

    boolean registerFaq(FAQDto faqDto);

    List<FAQDto> readFaq(int groupNo, int categoryNo);
    
    boolean deleteFaq(int groupNo, int faqNo);
}

// FaqServiceImpl

// Read

@Service
public class FAQServiceImpl implements FAQService{
// 서비스 인터페이스 구현체
    @Autowired
    FAQDao faqDao;
  // Mapping을 위함 Dao = Mapper
  
    @Override
    public List<FAQDto> readFaqAll(int groupNo) {
      // 특정 그룹의 Faq를 조회하기 위한 메서드, 파라미터로 groupNo를 받음
        List<FAQDto> faqList = faqDao.readFaqAll(groupNo);
      // @Mapper 어노테이션을 가진 FaqDao의 메서드로 groupNo를 파라미터로 넘김 => sql mapping에 이용
        return faqList;
      // FAQDto로 이루어진 리스트를 결과값으로 반환
    }

    @Override
    public List<FAQDto> readFaq(int groupNo, int categoryNo) {
      // 특정 그룹의 특정 카테고리 Faq를 조회하기 위한 메서드, 파라미터로 groupNo, categoryNo를 받음
      // 해당 공구 그릅의 특정 카테고리 => ex. 모델명 a 자전거 공동구매의 배송 문의
        List<FAQDto> faqList = faqDao.readFaq(groupNo, categoryNo);
      // @Mapper 어노테이션을 가진 FaqDao의 메서드로 groupNo, categoryNo를 파라미터로 넘김 => sql mapping에 이용
        return faqList;
      // FAQDto로 이루어진 리스트를 결과값으로 반환
    }
  //===========================================================================================================

  // Delete
  
    @Transactional
    // DB에 관여하는 서비스이므로 트랜잭션 처리
    @Override
    public boolean deleteFaq(int groupNo, int faqNo) {
      // 특정 그룹의 특정 Faq를 삭제하기 위한 메서드, 파리미터로 groupNo, faqNo를 받음
        int queryResult = 0;
      // sql을 수행항 결과에 따라 반환된 row값을 queryReult로 받음 결과에 따라 boolean 값을 반환
      
        FAQDto faq = faqDao.selectFaqDetail(groupNo, faqNo);
      // 특정 그룹 내 특정 Faq를 찾기 위한 selectFaqDetail 메서드
        if (faq != null) {
          // 특정 Faq가 null이 아니다 => 해당 Faq가 있다. => 삭제 실행
            queryResult = faqDao.deleteFaq(groupNo, faqNo);
        }
        return (queryResult == 1) ? true : false;
      // 삭제 쿼리문을 정상적으로 실행한다면 row값(1) 반환 결과에 따라 true or false 반환
    }
```



**3. Mapper**

```java
// FAQDao

@Mapper
public interface FAQDao {
// mapping을 위한 Mapper 인터페이스
    List<FAQDto> readFaq(int groupNo, int categoryNo);

    public FAQDto selectFaqDetail(int groupNo, int faqNo);

    int deleteFaq(int groupNo, int faqNo);
}

// faq_query.xml

// Read

    <select id="readFaq"  parameterType="int" resultType="com.dogather.pjtserver.dto.FAQDto">
        select * from faq  where group_no = #{groupNo} and category_no = #{categoryNo}
    </select>

    <select id="readFaqAll"  parameterType="int" resultType="com.dogather.pjtserver.dto.FAQDto">
        select * from faq  where group_no = #{groupNo}
    </select>

    <select id="selectFaqDetail"  parameterType="int" resultType="com.dogather.pjtserver.dto.FAQDto">
        select * from faq  where group_no = #{groupNo} and faq_no = #{faqNo}
    </select>
    //===========================================================================================================     

// Delete
      
    <delete id="deleteFaq" parameterType="int">
        delete from faq where group_no = #{groupNo} and faq_no = #{faqNo}
    </delete>

```

