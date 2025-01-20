package com.example.camping.controller;

import com.example.camping.entity.Users;
import com.example.camping.service.UserServiceImpl;
import jakarta.servlet.http.HttpSession;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@NoArgsConstructor
public class LoginController {

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
            session.setAttribute("loggedInUser", user);
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
            return "login/join";
        }
    }

   /* @PostMapping("find")
    public String find(){

    }*/
}
