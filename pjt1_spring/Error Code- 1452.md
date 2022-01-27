## Error Code: 1452

### 오늘 직면한 오류 : Error Code: 1452. Cannot add or update a child row: a foreign key constraint fails...

**상황** : 오늘 만들려 했던 테이블은 faq, faq_category였다. faq 테이블은 faq_category와 group(공동구매)를 참조하고 있었다. CRUD를 만드려다 C, U 로직을 한 API에서 구성하고 싶었다. 그래서 파라미터로 넘어오는 faqNo를 required를 false로 설정하고 해당 파라미터가 있다면 이미 있는 게시글이므로 Updatef, 없다면 Create로직을 수행하려 했다. 그런데 Java에서는 int x ==/!= null 을 체크할 수 없었다. 그래서 해당 파라미터를 Long타입으로 설정했다. (Long ==/!= null 체크가 가능했다.) 들어온 파라미터를 DTO에서도 같은 타입으로 받아야 했기에 기존에 설정되어 있던 int 타입을 Long으로 바꾸고 C, U 로직을 수행했지만 위에 기술한 오류를 맞닥뜨렸다. 이상하게도 log를 찍어봐도 1(Long)이 들어갔다면 DTO상에서는 1로 나왔는데 DB에 삽입이 되지 않았다. 그래서 해당 오류로 검색하면서 문제에 대한 원인을 분석했다.

**원인** : 참조 무결성 => 예를 들어 A테이블의 a 컬럼이 B테이블의 b 컬럼을 참조하고 있다면 두 컬럼은 항상 값이 일관되어야 한다. B테이블에서 값을 변경시켰다면 A 테이블에도 적용시켜야 한다. => 나는 faq와 faq_category의 값만 Long으로 변경했지만 faq가 참조하고 있는 테이블이 하나 더 있었다. group테이블의 참조되는키인 groupNo가 여전히 int 였기 때문에 해당 오류가 발생했을 것이다.

=> 외래 키(Foreign Key) 필드에 값을 삽입하거나 수정하려 할 때, 외래 키가 참조하는 주 키(Primary Key)에서 사용하는 값 이외의 값이 들어가면 이 에러를 반환한다.

=> 이 때는 주 키에 속한 값으로 외래 키를 수정하거나 삽입해야 한다.



**해결** : 처음 입력되는 두 파라미터 faqNo, groupNo중 faqNo는 Long타입으로 groupNo는 int로 설정했다. Long타입으로 null 체킹을 하고 싶었지만 전체 ERD상 연관되는 테이블들이 많아 함부로 값을 바꾸려했을 때 발생할 오류를 잡을 노력이 더 크다고 생각했다. 때문에 모두 int로 설정하고, faqNo는 null 체킹용으로 사용했다. 이후 faqNo가 있다면 int로 바꿔서 DTO에 set 해주었다.

