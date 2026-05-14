"use client"

import { ImageWithFallback } from "@/components/ui/image-with-fallback"
import Link from "next/link"
import { Heart, TrendingUp } from "lucide-react"
import { Card } from "@/components/ui/card"
import { cn } from "@/lib/utils"

interface RankingAnimal {
  id: string
  animalId?: number
  rank: number
  name: string
  imageUrl: string
  cheerTemperature: number
  maxCheerTemperature: number
}

interface RankingBannerProps {
  animals: RankingAnimal[]
}

const rankBadges = [
  { emoji: "🥇", label: "1st", bgColor: "bg-amber-400", textColor: "text-amber-900" },
  { emoji: "🥈", label: "2nd", bgColor: "bg-slate-300", textColor: "text-slate-700" },
  { emoji: "🥉", label: "3rd", bgColor: "bg-amber-600", textColor: "text-amber-100" },
]

export function RankingBanner({ animals }: RankingBannerProps) {
  return (
    <section className="bg-gradient-to-b from-primary/10 to-transparent py-8">
      <div className="max-w-6xl mx-auto px-4 md:px-6">
        <div className="flex flex-col xs:flex-row xs:items-center justify-between gap-3 mb-6">
          <div className="flex items-center gap-3">
            <div className="flex items-center justify-center w-8 h-8 md:w-10 md:h-10 rounded-xl bg-primary/20 shrink-0">
              <TrendingUp className="w-4 h-4 md:w-5 md:h-5 text-primary" />
            </div>
            <div>
              <h2 className="text-lg md:text-xl font-bold text-foreground">
                이번 주 응원 TOP 3
              </h2>
              <p className="text-xs md:text-sm text-muted-foreground whitespace-nowrap">가장 많은 응원을 받은 친구들</p>
            </div>
          </div>
          <span className="text-[10px] md:text-sm text-muted-foreground bg-secondary px-3 py-1.5 rounded-full w-fit whitespace-nowrap">
            매주 월요일 갱신
          </span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          {animals.map((animal, index) => {
            const badge = rankBadges[index]
            const progressPercent =
              (animal.cheerTemperature / animal.maxCheerTemperature) * 100

            return (
              <Link key={animal.id} href={`/animals/${animal.animalId || 1}`}>
                <Card
                  className={cn(
                    "relative overflow-hidden border-0 shadow-lg hover:shadow-xl transition-all duration-300 cursor-pointer group",
                    index === 0 && "ring-2 ring-primary/50 sm:scale-105 sm:z-10"
                  )}
                >
                {/* Rank Badge */}
                <div
                  className={cn(
                    "absolute top-3 left-3 z-10 flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-bold shadow-md",
                    badge.bgColor,
                    badge.textColor
                  )}
                >
                  <span className="text-base">{badge.emoji}</span>
                  <span>{badge.label}</span>
                </div>

                {/* Image */}
                <div className="relative aspect-[4/3]">
                  <ImageWithFallback
                    src={animal.imageUrl}
                    alt={animal.name}
                    fill
                    className="object-cover group-hover:scale-105 transition-transform duration-300"
                  />
                  {/* Gradient overlay for text readability */}
                  <div className="absolute inset-x-0 bottom-0 h-2/3 bg-gradient-to-t from-black/70 via-black/30 to-transparent" />
                </div>

                {/* Info Overlay */}
                <div className="absolute inset-x-0 bottom-0 p-4 space-y-3">
                  <h3 className="text-lg font-bold text-white">
                    {animal.name}
                  </h3>

                  {/* Temperature Bar */}
                  <div className="space-y-2">
                    <div className="flex items-center justify-between text-sm text-white/90">
                      <span className="flex items-center gap-1">
                        <Heart className="w-4 h-4 fill-primary text-primary" />
                        응원 온도
                      </span>
                      <span className="font-bold text-base">
                        {animal.cheerTemperature}°
                      </span>
                    </div>
                    <div className="h-2.5 bg-white/20 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-gradient-to-r from-primary to-accent rounded-full transition-all duration-500"
                        style={{ width: `${progressPercent}%` }}
                      />
                    </div>
                  </div>
                </div>
              </Card>
              </Link>
            )
          })}
        </div>
      </div>
    </section>
  )
}
