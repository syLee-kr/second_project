package com.example.camping.controller;

import com.example.camping.entity.Users;
import com.example.camping.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    /**
     * 프로필 페이지 조회
     * - 세션의 user 객체로 해당 사용자 정보를 불러와 모델에 담아준다.
     */
    @GetMapping
    public String getProfile(HttpSession session, Model model) {
        // 세션에서 user 객체 가져오기
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            // 로그인하지 않았다면 로그인 페이지로 리다이렉트
            return "redirect:/login";
        }

        // 사용자 정보 조회 (추가적인 최신 정보 필요 시)
        Users updatedUser = userService.userProfile(user.getUserId());
        model.addAttribute("user", updatedUser);

        // "profile.html" 템플릿 렌더링
        return "camp/profile";
        // 예: src/main/resources/templates/user/profile.html
    }

    /**
     * 프로필 수정 (POST)
     * - form에서 넘어온 nickname, phone, address 등을 업데이트
     */
    @PostMapping("/update")
    public String updateProfile(
            @RequestParam("nickname") String nickname,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            HttpSession session
    ) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // 기존 사용자 정보 가져오기
        Users existingUser = userService.userProfile(user.getUserId());

        // 업데이트할 필드 수정
        existingUser.setNickname(nickname);
        existingUser.setPhone(phone);
        existingUser.setAddress(address);

        // DB 저장
        userService.save(existingUser);

        // 세션의 user 객체 업데이트
        session.setAttribute("user", existingUser);

        // 수정 후 다시 프로필 페이지로
        return "redirect:/profile";
    }

    /**
     * 사용자 탈퇴 기능
     */
    @PostMapping("/delete")
    public String deleteUser(HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        userService.delete(user);

        // 세션 무효화 후 메인 페이지 등으로 리다이렉트
        session.invalidate();
        return "redirect:/";
    }
}
