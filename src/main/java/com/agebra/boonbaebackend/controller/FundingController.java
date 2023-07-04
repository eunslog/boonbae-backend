package com.agebra.boonbaebackend.controller;

import com.agebra.boonbaebackend.domain.PaymentMethod;
import com.agebra.boonbaebackend.domain.QnAType;
import com.agebra.boonbaebackend.domain.Users;
import com.agebra.boonbaebackend.dto.CategoryDto;
import com.agebra.boonbaebackend.dto.FundingDonateDto;
import com.agebra.boonbaebackend.dto.FundingDto;
import com.agebra.boonbaebackend.exception.NotFoundException;
import com.agebra.boonbaebackend.service.CategoryService;
import com.agebra.boonbaebackend.service.FundingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Tag(name = "FundingController", description = "Funding 관련한 컨트롤러 입니다")
@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/funding")
public class FundingController {
  private final FundingService fundingService;
  private final CategoryService categoryService;

  @PostMapping("/")
  public ResponseEntity addFunding(@RequestBody FundingDto.AddFunding dto, @AuthenticationPrincipal Users user) {
    fundingService.addFunding(dto, user);

    return ResponseEntity.ok().build();
  }

  @Secured("ADMIN")
  @PostMapping("/category/first") //첫 번째 카테고리 추가
  public ResponseEntity addFirstCategory(@RequestBody Map<String, String> map) {
    String name = map.get("name");
    if (name == null)
      throw new InputMismatchException("addFirstCategory: name은 필수입니다");
    categoryService.addFirstCategory(name);

    return ResponseEntity.ok().build();
  }

  @Secured("ADMIN")
  @PostMapping("/category/second") //두 번째 카테고리 추가
  public ResponseEntity addSecondCategory(@RequestBody @Valid CategoryDto.Add dto) {
    categoryService.addSecondCategory(dto);

    return ResponseEntity.ok().build();
  }

//  @GetMapping("/category") //전체조회
//  public ResponseEntity getCategoryList() {
//    categoryService.getAll();
//
//    return ResponseEntity.ok().build();
//  }

  // 펀딩 좋아요 추가 (Only User)
  @PostMapping("/{funding_pk}/likes")
  public ResponseEntity addLikeToFunding(@AuthenticationPrincipal Users user, @PathVariable("funding_pk") Long fundingPk) {
    fundingService.addLikeToFunding(user, fundingPk);
    return ResponseEntity.ok().build();
  }

  // 펀딩 좋아요 제거 (Only User)
  @DeleteMapping("/{funding_pk}/likes")
  public ResponseEntity removeLikeFromComment(@AuthenticationPrincipal Users user, @PathVariable("funding_pk") Long fundingPk) {
    fundingService.removeLikeFromFunding(user, fundingPk);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{funding_pk}/sponsor/CARD")
  public ResponseEntity addFundingDoanate(@RequestParam(value = "PaymentMethod",required = true)PaymentMethod paymentMethod,
                                          @AuthenticationPrincipal Users user,
                                          @PathVariable("funding_pk") Long fundingPk,
                                          @RequestBody FundingDonateDto.Request_All dto){
    Boolean checkPayment = fundingService.paymentCheck(dto,paymentMethod);
    if(!checkPayment){
      throw new NotFoundException("결제에 실패하였습니다 잘못된 정보가 없는지 확인해주세요");
    }
    fundingService.addDonateToFunding(user, fundingPk);
    return ResponseEntity.ok().build();
  }
  @GetMapping("/my")
  public ResponseEntity<List<FundingDto.MyFundingResponse>> MyFundingList(@AuthenticationPrincipal Users user){
     List<FundingDto.MyFundingResponse> userFundingList = fundingService.findAllDonateByUser(user);
     return ResponseEntity.ok().body(userFundingList);
  }
}
