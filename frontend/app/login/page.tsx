"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import Link from "next/link"
import { Eye, EyeOff, PawPrint } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardHeader } from "@/components/ui/card"
import { useAuth } from "@/lib/auth-context"
import { API_ENDPOINTS, apiRequest, oauthAuthorizationUrl } from "@/lib/api"
import { GoogleBrandIcon } from "@/components/oauth-brand-icons"

const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-={}\[\]:;"'<>,.?/]).+$/

type AuthPhase = "entry" | "login_password" | "signup_otp" | "signup_details" | "social_only"

type EmailStartNextStep = "SIGNUP_WITH_OTP" | "LOGIN_PASSWORD" | "SOCIAL_LOGIN_ONLY"

type EmailStartRes = {
  exists: boolean
  nextStep: EmailStartNextStep
}

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export default function LoginPage() {
  const router = useRouter()
  const { user, establishSessionFromAccessToken } = useAuth()

  const [phase, setPhase] = useState<AuthPhase>("entry")
  const [email, setEmail] = useState("")
  const [emailError, setEmailError] = useState("")

  const [password, setPassword] = useState("")
  const [showPassword, setShowPassword] = useState(false)
  const [passwordError, setPasswordError] = useState("")

  const [otpCode, setOtpCode] = useState("")
  const [verificationToken, setVerificationToken] = useState<string | null>(null)

  const [nickname, setNickname] = useState("")
  const [realname, setRealname] = useState("")
  const [signupPassword, setSignupPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [showSignupPassword, setShowSignupPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [fieldErrors, setFieldErrors] = useState({
    nickname: "",
    realname: "",
    signupPassword: "",
    confirmPassword: "",
  })

  const [error, setError] = useState("")
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    if (user) {
      router.replace("/")
    }
  }, [user, router])

  const validateEmail = (value: string) => {
    const trimmed = value.trim()
    if (!trimmed) return "이메일을 입력해주세요."
    if (!EMAIL_RE.test(trimmed)) return "올바른 이메일 형식이 아닙니다."
    return ""
  }

  const validateSignupPassword = (value: string) => {
    if (!value.trim()) return "비밀번호는 필수 입력값입니다."
    if (value.length < 8 || value.length > 16) return "비밀번호는 8~16자 사이여야 합니다."
    if (!PASSWORD_PATTERN.test(value)) {
      return "비밀번호는 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다."
    }
    return ""
  }

  const resetToEntry = () => {
    setPhase("entry")
    setPassword("")
    setOtpCode("")
    setVerificationToken(null)
    setSignupPassword("")
    setConfirmPassword("")
    setNickname("")
    setRealname("")
    setFieldErrors({ nickname: "", realname: "", signupPassword: "", confirmPassword: "" })
    setPasswordError("")
    setError("")
  }

  const handleEmailStart = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    const err = validateEmail(email)
    setEmailError(err)
    if (err) return

    setIsLoading(true)
    const { data, error: apiError } = await apiRequest<EmailStartRes>(API_ENDPOINTS.emailStart, {
      method: "POST",
      body: JSON.stringify({ email: email.trim() }),
    })
    setIsLoading(false)

    if (apiError || !data) {
      setError(apiError || "요청에 실패했습니다.")
      return
    }

    if (data.nextStep === "LOGIN_PASSWORD") {
      setPhase("login_password")
      return
    }

    if (data.nextStep === "SOCIAL_LOGIN_ONLY") {
      setPhase("social_only")
      return
    }

    if (data.nextStep === "SIGNUP_WITH_OTP") {
      setIsLoading(true)
      const sendRes = await apiRequest<void>(API_ENDPOINTS.emailSendOtp, {
        method: "POST",
        body: JSON.stringify({ email: email.trim() }),
      })
      setIsLoading(false)
      if (sendRes.error) {
        setError(sendRes.error)
        return
      }
      setPhase("signup_otp")
    }
  }

  const handleResendOtp = async () => {
    setError("")
    setIsLoading(true)
    const sendRes = await apiRequest<void>(API_ENDPOINTS.emailSendOtp, {
      method: "POST",
      body: JSON.stringify({ email: email.trim() }),
    })
    setIsLoading(false)
    if (sendRes.error) {
      setError(sendRes.error)
      return
    }
    setError("")
    alert("인증 메일을 다시 보냈습니다.")
  }

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    if (!/^\d{6}$/.test(otpCode)) {
      setError("인증 코드는 6자리 숫자입니다.")
      return
    }
    setIsLoading(true)
    const { data, error: apiError } = await apiRequest<{ verifyToken: string }>(API_ENDPOINTS.emailVerify, {
      method: "POST",
      body: JSON.stringify({ email: email.trim(), code: otpCode }),
    })
    setIsLoading(false)
    if (apiError || !data?.verifyToken) {
      setError(apiError || "인증에 실패했습니다.")
      return
    }
    setVerificationToken(data.verifyToken)
    setPhase("signup_details")
  }

  const handleEmailLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    if (!password.trim()) {
      setPasswordError("비밀번호를 입력해주세요.")
      return
    }
    setPasswordError("")
    setIsLoading(true)
    const { data, error: apiError, status } = await apiRequest<{ tokenType: string; accessToken: string }>(
      API_ENDPOINTS.emailLogin,
      {
        method: "POST",
        body: JSON.stringify({ email: email.trim(), password }),
      }
    )
    setIsLoading(false)
    if (apiError || !data) {
      if (status === 401) {
        setError("이메일 또는 비밀번호가 올바르지 않습니다.")
      } else {
        setError(apiError || "로그인에 실패했습니다.")
      }
      return
    }
    const session = await establishSessionFromAccessToken(data.accessToken, email.trim())
    if (!session.success) {
      setError(session.error || "로그인에 실패했습니다.")
      return
    }
    router.push("/")
  }

  const handleSignupComplete = async (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    const nextErrors = {
      nickname: !nickname.trim() ? "닉네임은 필수 입력값입니다." : "",
      realname: !realname.trim() ? "이름은 필수 입력값입니다." : "",
      signupPassword: validateSignupPassword(signupPassword),
      confirmPassword:
        !confirmPassword.trim()
          ? "비밀번호 확인은 필수입니다."
          : signupPassword !== confirmPassword
            ? "비밀번호가 일치하지 않습니다."
            : "",
    }
    setFieldErrors(nextErrors)
    if (Object.values(nextErrors).some(Boolean)) {
      setError("입력값을 확인해주세요.")
      return
    }
    if (!verificationToken) {
      setError("인증 정보가 없습니다. 처음부터 다시 시도해주세요.")
      return
    }

    setIsLoading(true)
    const { data, error: apiError } = await apiRequest<{ tokenType: string; accessToken: string }>(
      API_ENDPOINTS.emailSignup,
      {
        method: "POST",
        body: JSON.stringify({
          verificationToken,
          password: signupPassword,
          nickname: nickname.trim(),
          realname: realname.trim(),
        }),
      }
    )
    setIsLoading(false)
    if (apiError || !data) {
      setError(apiError || "회원가입에 실패했습니다.")
      return
    }
    const session = await establishSessionFromAccessToken(data.accessToken, email.trim())
    if (!session.success) {
      setError(session.error || "로그인 처리에 실패했습니다.")
      return
    }
    router.push("/")
  }

  const startOAuth = (provider: "naver" | "google") => {
    window.location.href = oauthAuthorizationUrl(provider)
  }

  const showEmailField = phase === "entry"
  const emailReadonly = phase !== "entry"

  return (
    <div className="min-h-screen bg-background flex items-center justify-center px-4 py-8">
      <Card className="w-full max-w-md border-0 shadow-xl">
        <CardHeader className="text-center pb-2">
          <Link href="/" className="inline-flex items-center justify-center gap-2 mb-2 md:mb-4">
            <div className="flex items-center justify-center w-10 h-10 md:w-12 md:h-12 rounded-xl bg-primary text-primary-foreground">
              <PawPrint className="w-5 h-5 md:w-6 md:h-6 fill-current" />
            </div>
          </Link>
          <h1 className="text-xl md:text-2xl font-bold text-foreground">로그인&amp;회원가입</h1>
          <p className="text-xs md:text-sm text-muted-foreground mt-1">
            이메일 또는 소셜 계정으로 펫미팅에 참여해요
          </p>
        </CardHeader>
        <CardContent className="pt-2 md:pt-4 space-y-5">
          {emailReadonly && (
            <div className="flex items-center justify-between gap-2 rounded-xl bg-secondary/50 px-3 py-2 text-sm">
              <span className="truncate text-foreground font-medium">{email.trim()}</span>
              <button
                type="button"
                onClick={resetToEntry}
                className="shrink-0 text-xs text-primary font-medium hover:underline"
              >
                변경
              </button>
            </div>
          )}

          {showEmailField && (
            <form onSubmit={handleEmailStart} className="space-y-3">
              <div className="space-y-1.5">
                <label htmlFor="email" className="text-sm font-medium text-foreground">
                  이메일
                </label>
                <Input
                  id="email"
                  type="email"
                  autoComplete="email"
                  value={email}
                  onChange={(e) => {
                    const v = e.target.value
                    setEmail(v)
                    if (!v.trim()) setEmailError("")
                    else setEmailError(validateEmail(v))
                  }}
                  placeholder="example@email.com"
                  className="h-12 rounded-xl bg-secondary/50 border-0"
                  disabled={isLoading}
                />
                {emailError && <p className="text-sm text-destructive">{emailError}</p>}
              </div>
              {error && phase === "entry" && (
                <div className="p-3 rounded-xl bg-destructive/10 text-destructive text-sm text-center">{error}</div>
              )}
              <Button
                type="submit"
                className="w-full h-12 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90 text-base font-semibold"
                disabled={isLoading}
              >
                {isLoading ? "처리 중..." : "이메일로 시작하기"}
              </Button>
            </form>
          )}

          {phase === "login_password" && (
            <form onSubmit={handleEmailLogin} className="space-y-3">
              <div className="space-y-1.5">
                <label htmlFor="password" className="text-sm font-medium text-foreground">
                  비밀번호
                </label>
                <div className="relative">
                  <Input
                    id="password"
                    type={showPassword ? "text" : "password"}
                    autoComplete="current-password"
                    value={password}
                    onChange={(e) => {
                      setPassword(e.target.value)
                      setPasswordError("")
                    }}
                    placeholder="비밀번호를 입력하세요"
                    className="h-12 rounded-xl bg-secondary/50 border-0 pr-12"
                    disabled={isLoading}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-4 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                  >
                    {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
                {passwordError && <p className="text-sm text-destructive">{passwordError}</p>}
              </div>
              {error && (
                <div className="p-3 rounded-xl bg-destructive/10 text-destructive text-sm text-center">{error}</div>
              )}
              <Button
                type="submit"
                className="w-full h-12 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90 text-base font-semibold"
                disabled={isLoading}
              >
                {isLoading ? "로그인 중..." : "로그인"}
              </Button>
            </form>
          )}

          {phase === "signup_otp" && (
            <form onSubmit={handleVerifyOtp} className="space-y-3">
              <p className="text-sm text-muted-foreground">
                입력하신 이메일로 보낸 <span className="font-medium text-foreground">6자리 인증 코드</span>를 입력해주세요.
              </p>
              <div className="space-y-1.5">
                <label htmlFor="otp" className="text-sm font-medium text-foreground">
                  인증 코드
                </label>
                <Input
                  id="otp"
                  inputMode="numeric"
                  maxLength={6}
                  value={otpCode}
                  onChange={(e) => setOtpCode(e.target.value.replace(/\D/g, "").slice(0, 6))}
                  placeholder="000000"
                  className="h-12 rounded-xl bg-secondary/50 border-0 tracking-widest text-center text-lg"
                  disabled={isLoading}
                />
              </div>
              {error && (
                <div className="p-3 rounded-xl bg-destructive/10 text-destructive text-sm text-center">{error}</div>
              )}
              <Button
                type="submit"
                className="w-full h-12 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90 text-base font-semibold"
                disabled={isLoading}
              >
                {isLoading ? "확인 중..." : "인증하기"}
              </Button>
              <button
                type="button"
                onClick={handleResendOtp}
                disabled={isLoading}
                className="w-full text-sm text-muted-foreground hover:text-foreground underline-offset-2 hover:underline"
              >
                인증 메일 다시 보내기
              </button>
            </form>
          )}

          {phase === "signup_details" && (
            <form onSubmit={handleSignupComplete} className="space-y-3 md:space-y-4">
              <div className="space-y-1.5">
                <label htmlFor="nickname" className="text-sm font-medium text-foreground">
                  닉네임
                </label>
                <Input
                  id="nickname"
                  value={nickname}
                  onChange={(e) => {
                    setNickname(e.target.value)
                    setFieldErrors((p) => ({ ...p, nickname: "" }))
                  }}
                  placeholder="닉네임"
                  className="h-12 rounded-xl bg-secondary/50 border-0"
                  disabled={isLoading}
                />
                {fieldErrors.nickname && <p className="text-sm text-destructive">{fieldErrors.nickname}</p>}
              </div>
              <div className="space-y-1.5">
                <label htmlFor="realname" className="text-sm font-medium text-foreground">
                  이름
                </label>
                <Input
                  id="realname"
                  value={realname}
                  onChange={(e) => {
                    setRealname(e.target.value)
                    setFieldErrors((p) => ({ ...p, realname: "" }))
                  }}
                  placeholder="실명"
                  className="h-12 rounded-xl bg-secondary/50 border-0"
                  disabled={isLoading}
                />
                {fieldErrors.realname && <p className="text-sm text-destructive">{fieldErrors.realname}</p>}
              </div>
              <div className="space-y-1.5">
                <label htmlFor="signupPassword" className="text-sm font-medium text-foreground">
                  비밀번호
                </label>
                <div className="relative">
                  <Input
                    id="signupPassword"
                    type={showSignupPassword ? "text" : "password"}
                    value={signupPassword}
                    onChange={(e) => {
                      setSignupPassword(e.target.value)
                      setFieldErrors((p) => ({
                        ...p,
                        signupPassword: validateSignupPassword(e.target.value),
                        confirmPassword:
                          confirmPassword && e.target.value !== confirmPassword
                            ? "비밀번호가 일치하지 않습니다."
                            : "",
                      }))
                    }}
                    placeholder="8~16자, 대·소문자·숫자·특수문자"
                    className="h-12 rounded-xl bg-secondary/50 border-0 pr-12"
                    disabled={isLoading}
                  />
                  <button
                    type="button"
                    onClick={() => setShowSignupPassword(!showSignupPassword)}
                    className="absolute right-4 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                  >
                    {showSignupPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
                {fieldErrors.signupPassword && (
                  <p className="text-sm text-destructive">{fieldErrors.signupPassword}</p>
                )}
              </div>
              <div className="space-y-1.5">
                <label htmlFor="confirmPassword" className="text-sm font-medium text-foreground">
                  비밀번호 확인
                </label>
                <div className="relative">
                  <Input
                    id="confirmPassword"
                    type={showConfirmPassword ? "text" : "password"}
                    value={confirmPassword}
                    onChange={(e) => {
                      setConfirmPassword(e.target.value)
                      setFieldErrors((p) => ({
                        ...p,
                        confirmPassword:
                          !e.target.value.trim()
                            ? "비밀번호 확인은 필수입니다."
                            : signupPassword !== e.target.value
                              ? "비밀번호가 일치하지 않습니다."
                              : "",
                      }))
                    }}
                    placeholder="비밀번호 재입력"
                    className="h-12 rounded-xl bg-secondary/50 border-0 pr-12"
                    disabled={isLoading}
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute right-4 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
                  >
                    {showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
                {fieldErrors.confirmPassword && (
                  <p className="text-sm text-destructive">{fieldErrors.confirmPassword}</p>
                )}
              </div>
              {error && (
                <div className="p-3 rounded-xl bg-destructive/10 text-destructive text-sm text-center">{error}</div>
              )}
              <Button
                type="submit"
                className="w-full h-12 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90 text-base font-semibold"
                disabled={isLoading}
              >
                {isLoading ? "가입 중..." : "회원가입 완료"}
              </Button>
            </form>
          )}

          {phase === "social_only" && (
            <div className="space-y-2 rounded-xl bg-muted/50 p-3 text-sm text-muted-foreground text-center">
              이 이메일은 구글로 가입한 회원입니다.
              <br />
              아래 "구글로 시작하기" 버튼으로 로그인해주세요.
            </div>
          )}

          {(phase === "entry" || phase === "social_only") && (
            <div className="space-y-2">
              <Button
                type="button"
                variant="outline"
                className="w-full h-12 rounded-xl border-border/80 bg-background hover:bg-secondary/40 text-foreground"
                onClick={() => startOAuth("google")}
                disabled={isLoading}
              >
                <GoogleBrandIcon className="mr-2" />
                구글로 시작하기
              </Button>
            </div>
          )}


          <div className="pt-2 text-center">
            <Link href="/" className="text-xs text-muted-foreground hover:text-foreground transition-colors">
              홈으로 돌아가기
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
