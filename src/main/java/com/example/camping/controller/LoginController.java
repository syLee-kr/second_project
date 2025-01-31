package com.example.camping.controller;

import com.example.camping.entity.Users;
import com.example.camping.service.UserServiceImpl;
import jakarta.servlet.http.HttpSession;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UserServiceImpl userService;

    @GetMapping("/login")
    public String login(HttpSession session){
        Users user = (Users) session.getAttribute("user");
        if (user != null) {
            return "redirect:/camp";
        } else {
            return "login/login";
        }
    }

    @PostMapping("/login")
    public String loginSummit(@RequestParam String userId,
                              @RequestParam String password,
                              HttpSession session) {
        try {
            Users user = userService.login(userId, password);
            session.setAttribute("user", user);
            System.out.println("user:" + session.getAttribute("user"));
            return "redirect:/camp";
        } catch (RuntimeException e) {
            return "redirect:/login?error=" + e.getMessage();  // 로그인 실패 시 에러 메시지 전달
        }
    }

    @GetMapping("/join")
    public String join(HttpSession session){
        Users user = (Users) session.getAttribute("user");
        if (user != null) {
            return "redirect:/camp";
        } else {
            return "login/join";
        }
    }

    @PostMapping("/join")
    public String joinSummit(@ModelAttribute Users user,
                             @RequestParam String confirmPassword,
                             Model model) {
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "login/join";  // 회원가입 페이지로 다시 이동
        }

        try {
            userService.registerUser(user);
            return "redirect:/login";  // 가입 성공 후 로그인 페이지로 이동
        } catch (Exception e) {
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return "login/join";  // 회원가입 페이지로 다시 이동
        }
    }

    @GetMapping("find")
    public String find(HttpSession session) {
        Users user = (Users) session.getAttribute("user");
        if (user != null) {
            return "redirect:/camp";
        } else {
            return "login/find";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 무효화 (로그아웃 처리)
        return "redirect:/login?logout=true"; // 로그아웃 후 로그인 페이지로 리디렉션
    }

    @PostMapping("/findId")
    public String findId(@RequestParam("phone") String phone, Model model) {
        Users user = userService.findByPhone(phone);
        if (user != null) {
            String userId = user.getUserId(); // 아이디 직접 가져오기
            model.addAttribute("foundUserId", userId); // 모델에 아이디 추가
        } else {
            model.addAttribute("findIdError", "등록되지 않은 전화번호입니다.");
        }
        return "login/find";
    }

    @PostMapping("/findPassword")
    public String findPassword(@RequestParam("email") String email, Model model) {
        Users user = userService.userProfile(email);
        if (user != null) {
            userService.createPasswordResetToken(user);
            model.addAttribute("findPasswordMessage", "입력하신 이메일로 비밀번호 재설정 링크를 전송했습니다.");
        } else {
            model.addAttribute("findPasswordError", "등록되지 않은 이메일입니다.");
        }
        return "login/find";
    }


    // 비밀번호 재설정 폼 표시
    @GetMapping("/resetPassword")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        Optional<Users> userOpt = userService.findByResetToken(token);
        if (userOpt.isPresent() && userService.isResetTokenValid(userOpt.get())) {
            model.addAttribute("token", token);
            return "login/resetPassword";
        } else {
            model.addAttribute("resetError", "비밀번호 재설정 링크가 유효하지 않거나 만료되었습니다.");
            return "login/resetPassword";
        }
    }

    // 비밀번호 재설정 처리
    @PostMapping("/resetPassword")
    public String resetPassword(@RequestParam("token") String token,
                                @RequestParam("password") String password,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("resetError", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("token", token);
            return "login/resetPassword";
        }

        Optional<Users> userOpt = userService.findByResetToken(token);
        if (userOpt.isPresent() && userService.isResetTokenValid(userOpt.get())) {
            Users user = userOpt.get();
            userService.resetPassword(user, password);
            model.addAttribute("resetMessage", "비밀번호가 성공적으로 재설정되었습니다.");
            return "login/resetPassword";
        } else {
            model.addAttribute("resetError", "비밀번호 재설정 링크가 유효하지 않거나 만료되었습니다.");
            return "login/resetPassword";
        }
    }
}
