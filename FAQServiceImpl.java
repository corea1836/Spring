package com.dogather.pjtserver.service;

import com.dogather.pjtserver.dao.FAQDao;
import com.dogather.pjtserver.dto.FAQDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class FAQServiceImpl implements FAQService{

    @Autowired
    FAQDao faqDao;

    @Transactional
    @Override
    public boolean registerFaq(FAQDto faqDto) {
        int queryResult = 0;

        log.info(faqDto.toString());
        if (faqDto.getFaqNo() != 0){

            log.info("수정 실행!!!!!!!!");
            queryResult = faqDao.updateFaq(faqDto);

            log.info("=============");
            log.info("수정 완료");


        } else {
            log.info("생성 실행 !!!!!!!!");
            queryResult = faqDao.createFaq(faqDto);

            log.info("================");
            log.info("생성 완료");
        }
        return (queryResult == 1) ? true : false;
    }

    @Override
    public List<FAQDto> readFaq(int groupNo, int categoryNo) {
        List<FAQDto> faqList = faqDao.readFaq(groupNo, categoryNo);
        return faqList;
    }

    @Transactional
    @Override
    public boolean deleteFaq(int groupNo, int faqNo) {
        int queryResult = 0;

        FAQDto faq = faqDao.selectFaqDetail(groupNo, faqNo);
        if (faq != null) {
            queryResult = faqDao.deleteFaq(groupNo, faqNo);
        }
        return (queryResult == 1) ? true : false;
    }
}
