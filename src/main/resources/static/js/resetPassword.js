document.addEventListener("DOMContentLoaded", function() {
    let sendCodeBtn = document.getElementById("sendCodeBtn");

    // 인증코드 발송 버튼 클릭 시
    if (sendCodeBtn) {
        sendCodeBtn.addEventListener("click", function(event) {
            event.preventDefault();  // 폼 제출을 막고, Ajax로 인증코드를 발송

            // 서버에 인증코드 발송 요청
            fetch("/sendCode", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                },
                body: "userId=" + document.getElementById("userId").value + "&email=" + document.getElementById("email-password").value
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert("인증코드가 발송되었습니다.");
					window.location.href ="/login"; // 로그인 페이지로
                    // 인증코드가 비밀번호로 설정되는 메커니즘 실행
                    resetPasswordWithCode(data.code); // 서버에서 받은 인증코드로 비밀번호 변경
                } else {
                    alert("인증코드 발송에 실패했습니다.");
                }
            })
            .catch(error => {
                console.error('Error:', error);
            });
        });
    }

    // 인증코드를 바로 비밀번호로 설정하는 함수
    function resetPasswordWithCode(code) {
        // 인증코드를 비밀번호로 설정
        fetch("/reset-password", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
            },
            body: "newPassword=" + code
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert("비밀번호가 성공적으로 변경되었습니다.");
            } else {
                alert("비밀번호 변경에 실패했습니다.");
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
    }
});
