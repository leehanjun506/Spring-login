# Summary
##  도메인 = 화면, UI, 기술 인프라 등등의 영역을 제외한 시스템이 구현해야 하는 핵심 비즈니스 업무 영역을 말함
</br>

## 로그인 상태 유지하기
### 로그인의 상태를 어떻게 유지할 수 있을까? 쿠키를 사용해보자
### 쿠키에는 영속 쿠키와 세션 쿠키가 있다.
* 영속 쿠키: 만료 날짜를 입력하면 해당 날짜까지 유지
* 세션 쿠키: 만료 날짜를 생략하면 브라우저 종료시 까지만 유지

### 브라우저 종료시 로그아웃이 되길 기대하므로, 우리에게 필요한 것은 세션 쿠키이다.
</br>

### 로그인에 성공하면 쿠키를 생성하고 response객체에 담는다. 세션 쿠키이기 때문에 웹 브라우저는 종료 전까지 쿠키를 지니고 있을 것이다.

### 또한 서버에서 해당 쿠키의 종료 날짜를 0으로 지정한다면 로그아웃을 시킬 수 있다.


## 보안 문제
### 위와 같은 방식은 보안 문제를 지니고 있다.
### 쿠키 값은 임의로 변경할 수 있다.
### 클라이언트가 쿠키를 강제로 변경하면 다른 사용자가 된다.
### 실제 웹브라우저 개발자모드 Application Cookie 변경으로 확인
</br>

## 대안
### 쿠키에 중요한 값을 노출하지 않고, 사용자 별로 예측 불가능한 임의의 토큰(랜덤 값)을 노출하고, 서버에서 토큰과 사용자 id를 매핑해서 인식한다. 그리고 서버에서 토큰을 관리한다.
### 토큰은 해커가 임의의 값을 넣어도 찾을 수 없도록 예상 불가능 해야 한다. 
### 해커가 토큰을 털어가도 시간이 지나면 사용할 수 없도록 서버에서 해당 토큰의 만료시간을 짧게(예: 30분) 유지한다. 또는 해킹이 의심되는 경우 서버에서 해당 토큰을 강제로 제거하면 된다.
</br>

## 로그인 처리하기 - 세션 동작 방식

### 앞서 쿠키에 중요한 정보를 보관하는 방법은 여러가지 보안 이슈가 있었다. 이 문제를 해결하려면 결국 중요한 정보를 모두 서버에 저장해야 한다. 
### 그리고 클라이언트와 서버는 추정 불가능한 임의의 식별자 값으로 연결해야 한다.
### 이렇게 서버에 중요한 정보를 보관하고 연결을 유지하는 방법을 세션이라 한다.

## 세션 동작 방식
1. 사용자가 로그인 정보를 전달하면 서버에서 해당 사용자가 맞는지 확인하고 추정 불가능한 세션id를 생성한다.
2. 생성된 세션 id와 세션에 보관할 값을 서버의 세션 저장소에 보관한다.
3. 세션 id를 응답 쿠키로 전달한다. 이는 추정 불가능한 값이다.
4. 로그인 이후 접근 시 클라이언트는 항상 세션id 쿠키를 전달한다.
5. 서버에서는 클라이언트가 전달한 쿠키 정보로 세션 저장소를 조회해서 로그인시 보관한 세션 정보를 사용한다.

### 세션을 사용해서 서버에서 중요한 정보를 관리하게 되었다. 덕분에 다음과 같은 보안 문제들을 해결할 수 있다.
### 쿠키 값을 변조 가능, 예상 불가능한 복잡한 세션Id를 사용한다.
### 쿠키에 보관하는 정보는 클라이언트 해킹시 털릴 가능성이 있다. 세션Id가 털려도 여기에는 중요한 정보가 없다.
### 쿠키 탈취 후 사용 해커가 토큰을 털어가도 시간이 지나면 사용할 수 없도록 서버에서 세션의
### 만료시간을 짧게(예: 30분) 유지한다. 또는 해킹이 의심되는 경우 서버에서 해당 세션을 강제로 제거하면 된다
</br>

## Code Feature
```

//세션이 있으면 있는 세션 반환, 없으면 신규 세션을 생성
(파라미터가 true(default일때만))
HttpSession session = request.getSession();

//세션에 로그인 회원 정보 보관
session.setAttribute(SessionConst.LOGIN_MEMBER,loginMember);
session.invalidate(); //세션을 제거한다.
```

```
@GetMapping("/")
public String homeLoginV3Spring(
 @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,Model model) {
    //세션에 회원 데이터가 없으면 home
    if (loginMember == null) {
    return "home";
    }
    //세션이 유지되면 로그인으로 이동
    model.addAttribute("member", loginMember);
    return "loginHome";
}
```
### @SessionAttribute를 사용해 세션이름과 매칭 된 Member객체의 session유무를 확인한다.
</br>

## 세션 타임아웃 설정
### 세션은 사용자가 로그아웃을 직접 호출해서 session.invalidate() 가 호출 되는 경우에 삭제된다. 
### 그런데 대부분의 사용자는 로그아웃을 선택하지 않고, 그냥 웹 브라우저를 종료한다. 문제는 HTTP가 비연결성(ConnectionLess)이므로 서버 입장에서는 해당 사용자가 웹 브라우저를 종료한 것인지 아닌지를 인식할 수 없다. 
### 따라서 서버에서 세션 데이터를 언제 삭제해야 하는지 판단하기가 어렵기 때문에 사용자가 세션 타임아웃을 설정해줘야 한다.
</br>

## 세션 타임아웃 설정
### 스프링 부트로 글로벌 설정
```
application.properties
server.servlet.session.timeout=60 : 60초, 기본은 1800(30분)
```
</br>

### 특정 세션 단위로 시간 설정
```
session.setMaxInactiveInterval(1800); //1800초
```

## 세션 타임아웃 발생
### 세션의 타임아웃 시간은 해당 세션과 관련된 JSESSIONID(cookie) 를 전달하는 HTTP 요청이 있으면 현재 시간으로 다시 초기화 된다. 
### 이렇게 초기화 되면 세션 타임아웃으로 설정한 시간동안 세션을 추가로 사용할 수 있다.
### session.getLastAccessedTime() : 최근 세션 접근 시간
### LastAccessedTime 이후로 timeout 시간이 지나면, WAS가 내부에서 해당 세션을 제거한다.
</br>

---
</br>

## 서블릿 필터
### 공통 관심 사항
### 로그인을 하지 않은 사용자가 URL를 직접 호출하면 관련 화면에 들어갈 수 있다. 때문에 모든 컨트롤러 로직에 공통으로 로그인 여부를 확인해야 한다.
### 이렇게 애플리케이션 여러 로직에서 공통으로 관심이 있는 것을 공통 관심사(cross-cutting concern)라고 한다.
### 웹과 관련된 공통 관심사는 스프링의 AOP보단 서블릿 필터 또는 스프링 인터셉터를 사용하는 것이 좋다.
### 웹과 관련된 공통 관심사를 처리할 때는 HTTP의 헤더나 URL의 정보들이 필요한다, 서블릿 필터나 스프링 인터셉터는 HttpServletRequest를 제공한다.
</br>

### 필터 흐름
#### HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러

### 필터 제한
#### HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러 //로그인 사용자
#### HTTP 요청 -> WAS -> 필터(적절하지 않은 요청이라 판단, 서블릿 호출X) //비 로그인 사용자
</br>

### 필터 체인
#### HTTP 요청 -> WAS -> 필터1 -> 필터2 -> 필터3 -> 서블릿 -> 컨트롤러
#### 필터는 체인으로 구성되는데, 중간에 필터를 자유롭게 추가할 수 있다. 예를 들어서 로그를 남기는 필터를 먼저 적용하고, 그 다음에 로그인 여부를 체크하는 필터를 만들 수 있다.
</br>

---
</br>

## 스프링 인터셉터 : 웹과 관련된 공통 관심 사항을 해결하는 기술

### 스프링 인터셉터 흐름
#### HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터 -> 컨트롤러
* 스프링 인터셉터는 디스패처 서블릿과 컨트롤러 사이에서 컨트롤러 호출 직전에 호출 된다.
* 스프링 인터셉터는 스프링 MVC가 제공하는 기능이기 때문에 결국 디스패처 서블릿 이후에 등장하게 된다. 스프링 MVC의 시작점이 디스패처 서블릿이라고 생각해보면 이해가 될 것이다.
* 스프링 인터셉터에도 URL 패턴을 적용할 수 있는데, 서블릿 URL 패턴과는 다르고, 매우 정밀하게 설정할 수 있다.

### 스프링 인터셉터 제한
#### HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터 -> 컨트롤러 //로그인 사용자
#### HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터(적절하지 않은 요청이라 판단, 컨트롤러 호출X) // 비 로그인 사용자
</br>

### 스프링 인터셉터 체인
#### HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 인터셉터1 -> 인터셉터2 -> 컨트롤러
</br>

### 스프링 인터셉터 인터페이스
### 스프링의 인터셉터를 사용하려면 HandlerInterceptor 인터페이스를 구현하면 된다.
```
public interface HandlerInterceptor {

default boolean preHandle(HttpServletRequest request, HttpServletResponse
response,Object handler) throws Exception {}

default void postHandle(HttpServletRequest request, HttpServletResponse
response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {}

default void afterCompletion(HttpServletRequest request, HttpServletResponse
response, Object handler, @Nullable Exception ex) throws Exception {}

}
```
* preHandle : 컨트롤러 호출 전에 호출된다. (더 정확히는 핸들러 어댑터 호출 전에 호출된다.) preHandle 의 응답값이 true 이면 다음으로 진행하고, false 이면 더는 진행하지 않는다. false인 경우 나머지 인터셉터는 물론이고, 핸들러 어댑터도 호출되지 않는다.
* postHandle : 컨트롤러 호출 후에 호출된다. (더 정확히는 핸들러 어댑터 호출 후에 호출된다.)
* afterCompletion : 뷰가 렌더링 된 이후에 호출된다.

### 예외가 발생시
* preHandle : 컨트롤러 호출 전에 호출된다.
* postHandle : 컨트롤러에서 예외가 발생하면 postHandle 은 호출되지 않는다.
* afterCompletion : afterCompletion 은 항상 호출된다. 이 경우 예외( ex )를 파라미터로 받아서 어떤 예외가 발생했는지 로그로 출력할 수 있다.

### WebConfig - 인터셉터 등록

```
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LogInterceptor())
        .order(1)
        .addPathPatterns("/**")
        .excludePathPatterns("/css/**", "/*.ico", "/error");
    }
//...
}
```
#### WebMvcConfigurer 가 제공하는 addInterceptors() 를 사용해서 인터셉터를 등록할 수 있다.
* registry.addInterceptor(new LogInterceptor()) : 인터셉터를 등록한다.
* order(1) : 인터셉터의 호출 순서를 지정한다. 낮을 수록 먼저 호출된다.
* addPathPatterns("/**") : 인터셉터를 적용할 URL 패턴을 지정한다.
* excludePathPatterns("/css/**", "/*.ico", "/error") : 인터셉터에서 제외할 패턴을 지정한다.

### 인증이라는 것은 컨트롤러 호출 전에만 호출되면 된다. 따라서 preHandle 만 구현하면 된다.