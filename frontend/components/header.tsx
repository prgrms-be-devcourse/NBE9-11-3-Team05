"use client"

import { useEffect, useMemo, useState } from "react"
import Link from "next/link"
import { Search, Bell, Heart, LogOut, PawPrint, Shield, User } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { useAuth } from "@/lib/auth-context"
import { API_ENDPOINTS, apiRequest, isAdminUser } from "@/lib/api"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"

interface HeaderProps {
  dailyHeartsRemaining?: number
  maxDailyHearts?: number
}

const DEFAULT_MAX_DAILY_HEARTS = 5

const extractRemainingToday = (payload: unknown): number | null => {
  if (typeof payload === "number") return payload
  if (typeof payload === "string") {
    const parsed = Number(payload)
    return Number.isFinite(parsed) ? parsed : null
  }
  if (!payload || typeof payload !== "object") return null

  const obj = payload as Record<string, unknown>
  const direct =
    obj.remainingToday ?? obj.remaining ?? obj.remainingCheers ?? (obj as any).heartsRemaining ?? (obj as any).dailyHeartsRemaining

  if (typeof direct === "number") return direct
  if (typeof direct === "string") {
    const parsed = Number(direct)
    return Number.isFinite(parsed) ? parsed : null
  }

  if (obj.data && typeof obj.data === "object") return extractRemainingToday(obj.data)
  if (obj.result && typeof obj.result === "object") return extractRemainingToday(obj.result)
  return null
}

export function Header({ dailyHeartsRemaining, maxDailyHearts }: HeaderProps) {
  const { user, logout, isLoading } = useAuth()
  const isAdmin = isAdminUser(user)
  const effectiveMax = maxDailyHearts ?? DEFAULT_MAX_DAILY_HEARTS
  const isControlled = dailyHeartsRemaining != null && maxDailyHearts != null
  const [uncontrolledRemaining, setUncontrolledRemaining] = useState(effectiveMax)

  const effectiveRemaining = useMemo(() => {
    if (isControlled) return Math.max(0, Math.min(effectiveMax, dailyHeartsRemaining!))
    return Math.max(0, Math.min(effectiveMax, uncontrolledRemaining))
  }, [dailyHeartsRemaining, effectiveMax, isControlled, uncontrolledRemaining])

  useEffect(() => {
    if (isControlled) return
    if (!user) {
      setUncontrolledRemaining(effectiveMax)
      return
    }

    let cancelled = false
    ;(async () => {
      const { data } = await apiRequest<unknown>(API_ENDPOINTS.cheersToday)
      const remainingToday = extractRemainingToday(data)
      if (remainingToday == null) return
      if (cancelled) return
      setUncontrolledRemaining(Math.max(0, Math.min(effectiveMax, remainingToday)))
    })()

    return () => {
      cancelled = true
    }
  }, [effectiveMax, isControlled, user])

  const handleLogout = async () => {
    await logout()
  }

  return (
    <header className="sticky top-0 z-50 bg-card/95 backdrop-blur-lg border-b border-border">
      <div className="max-w-6xl mx-auto px-3 md:px-6 py-3 md:py-4">
        <div className="flex items-center justify-between gap-1 md:gap-8">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-2">
            <div className="flex items-center justify-center w-8 h-8 md:w-10 md:h-10 rounded-xl bg-primary text-primary-foreground shrink-0">
              <PawPrint className="w-4 h-4 md:w-5 md:h-5 fill-current" />
            </div>
            <span className="hidden min-[420px]:block text-lg md:text-xl font-bold text-foreground whitespace-nowrap">펫미팅</span>
          </Link>

          {/* Navigation */}
          <nav className="flex items-center gap-0 md:gap-1">
            <Link href="/">
              <Button variant="ghost" size="sm" className="text-xs sm:text-sm text-foreground font-medium px-1.5 md:px-4">
                홈
              </Button>
            </Link>
            <Link href="/community">
              <Button variant="ghost" size="sm" className="text-xs sm:text-sm text-muted-foreground font-medium px-1.5 md:px-4">
                커뮤니티
              </Button>
            </Link>
            <Link href="/campaign">
              <Button variant="ghost" size="sm" className="text-xs sm:text-sm text-muted-foreground font-medium px-1.5 md:px-4">
                캠페인
              </Button>
            </Link>
          </nav>

          {/* Search & Actions */}
          <div className="flex items-center gap-2 md:gap-3">
            {/* 
            <div className="relative hidden sm:block">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                placeholder="검색어를 입력하세요"
                className="w-64 pl-10 bg-secondary/50 border-0 rounded-xl"
              />
            </div>
            */}
            
            {isLoading ? (
              <div className="h-8 w-24 rounded-xl bg-secondary/70 animate-pulse" />
            ) : user ? (
              <>
                {/* Daily Hearts Indicator */}
                <div className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 bg-primary/10 rounded-xl">
                  <div className="flex items-center gap-0.5">
                    {Array.from({ length: effectiveMax }).map((_, index) => (
                      <Heart 
                        key={index}
                        className={`w-4 h-4 transition-all ${
                          index < effectiveRemaining 
                            ? "fill-primary text-primary" 
                            : "text-primary/30"
                        }`} 
                      />
                    ))}
                  </div>
                  <span className="text-xs font-medium text-primary">
                    {effectiveRemaining}/{effectiveMax}
                  </span>
                </div>
                
                {/* <Button variant="ghost" size="icon" className="text-muted-foreground">
                  <Bell className="w-5 h-5" />
                </Button> */}
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="icon" className="text-muted-foreground">
                      <User className="w-5 h-5" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end" className="w-48">
                    <div className="px-2 py-1.5">
                      <p className="text-sm font-medium text-foreground">{user.nickname || user.name}</p>
                    </div>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem asChild>
                      <Link href="/profile" className="cursor-pointer">
                        <User className="w-4 h-4 mr-2" />
                        프로필
                      </Link>
                    </DropdownMenuItem>
                    {isAdmin && (
                      <DropdownMenuItem asChild>
                        <Link href="/admin" className="cursor-pointer">
                          <Shield className="w-4 h-4 mr-2" />
                          보호소 관리
                        </Link>
                      </DropdownMenuItem>
                    )}
                    <DropdownMenuSeparator />
                    <DropdownMenuItem 
                      className="text-destructive cursor-pointer"
                      onClick={handleLogout}
                    >
                      <LogOut className="w-4 h-4 mr-2" />
                      로그아웃
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </>
            ) : (
              <Link href="/login">
                <Button
                  size="sm"
                  className="bg-primary text-primary-foreground hover:bg-primary/90 rounded-xl px-2 sm:px-3 text-[11px] sm:text-sm h-8 md:h-9 whitespace-nowrap"
                >
                  로그인&amp;회원가입
                </Button>
              </Link>
            )}
          </div>
        </div>
      </div>
    </header>
  )
}
