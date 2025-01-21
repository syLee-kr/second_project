package com.example.camping.controller;

import com.example.camping.campingService.CampingService;
import com.example.camping.entity.Users;
import com.example.camping.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;

@Controller
public class CampingController {

	//가까운 캠핑장 5곳 추천 컨트롤러 
    @Autowired
    private CampingService campingService;

    @Autowired
    private UserRepository userRepository;

    //사용자 id기반으로 주소 조회 
    @GetMapping("/recommend")
    public String recommendCampingSites(Model model, @RequestParam("userId") String userId) {
        Optional<Users> user = userRepository.findById(userId);
        if (user.isPresent()) {
            Users userInfo = user.get();
            String userAddress = userInfo.getCity() + " " + userInfo.getDistrict() + " " + userInfo.getDetailedAddress();
            String recommendations = campingService.getRecommendations(userAddress);
            model.addAttribute("recommendations", recommendations);
        } else {
            model.addAttribute("error", "사용자를 찾을 수 없습니다.");
        }
        return "recommend";
    }
}
