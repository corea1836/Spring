package com.dogather.pjtserver.controller;

import com.dogather.pjtserver.dto.FAQDto;
import com.dogather.pjtserver.service.FAQService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class FAQController {

    @Autowired
    FAQService faqService;

    @RequestMapping(value = {"/faq/{groupNo}", "/faq/{groupNo}/{faqNo}"}, method = {RequestMethod.POST, RequestMethod.PUT})
    public ResponseEntity<JSONObject> registerFaq (@PathVariable(value = "faqNo", required = false) Long faqNo,
                                                   @PathVariable(value = "groupNo") int groupNo,
                                                   @RequestBody FAQDto faqDto){
        JSONObject jsonObj = new JSONObject();
        faqDto.setGroupNo(groupNo);
        try {
            if (faqNo != null) {
                faqDto.setFaqNo(faqNo.intValue());
            }
            boolean isRegistered = faqService.registerFaq(faqDto);
            jsonObj.append("Register result", isRegistered);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
            jsonObj.append("message", "시스템에 문제가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonObj);
        }
        return ResponseEntity.status(HttpStatus.OK).body(jsonObj);
    }


    @GetMapping("/{groupNo}/{categoryNo}")
    public ResponseEntity<List<FAQDto>> readFaq(@PathVariable("groupNo") int groupNo,
                                                @PathVariable("categoryNo") int categoryNo){

        List<FAQDto> readList = faqService.readFaq(groupNo, categoryNo);
        return ResponseEntity.status(HttpStatus.OK).body(readList);
    }


    @DeleteMapping("/faq/{groupNo}/{faqNo}")
    public ResponseEntity<String> deleteFaq(@PathVariable("groupNo") int groupNo,
                                            @PathVariable("faqNo") int faqNo) {
        JSONObject jsonObj = new JSONObject();
        try {
            boolean isDeleted = faqService.deleteFaq(groupNo, faqNo);
            jsonObj.append("Delete result", isDeleted);
        } catch (DataAccessException e) {
            e.printStackTrace();
            jsonObj.append("result", "데이터베이스 처리과정 중 문제가 발생하였습니다. 입력값을 다시 확인해주세요.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(jsonObj.toString());
        } catch (Exception e) {
            e.printStackTrace();
            jsonObj.append("message", "시스템에 문제가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonObj.toString());
        }
        return ResponseEntity.status(HttpStatus.OK).body(jsonObj.toString());
    }


}
