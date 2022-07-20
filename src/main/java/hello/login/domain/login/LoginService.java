package hello.login.domain.login;

import hello.login.domain.member.Member;
import hello.login.domain.member.MemberRepositroy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepositroy memberRepositroy;

    /**
     * @return null 로그인 실패
     */
    public Member login(String loginId, String password) {
//        Optional<Member> findMemberOptional = memberRepositroy.findByLoginId(loginId);
//        Member member = findMemberOptional.get();
//        if (member.getPassword().equals(password)) {
//            return member;
//        } else{
//            return null;
//        }
        return memberRepositroy.findByLoginId(loginId)
                .filter(m -> m.getPassword().equals(password))
                .orElse(null);
    }
}
