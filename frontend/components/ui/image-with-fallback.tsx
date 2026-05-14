"use client"

import { useState, useEffect } from "react"
import Image, { ImageProps } from "next/image"
import { PawPrint } from "lucide-react"
import { cn } from "@/lib/utils"

interface ImageWithFallbackProps extends Omit<ImageProps, "src"> {
  src?: string | null
  fallbackClassName?: string
  iconClassName?: string
}

export function ImageWithFallback({
  src,
  alt,
  className,
  fallbackClassName,
  iconClassName,
  ...props
}: ImageWithFallbackProps) {
  const [error, setError] = useState(false)

  // Reset error state when src changes
  useEffect(() => {
    setError(false)
  }, [src])

  const isFallback = error || !src || src === "null" || src === "undefined" || src === "/placeholder.svg"

  if (isFallback) {
    return (
      <div
        className={cn(
          "flex items-center justify-center bg-primary/5 text-primary w-full h-full",
          fallbackClassName,
          className
        )}
      >
        <PawPrint className={cn("w-1/4 h-1/4 opacity-40 fill-current", iconClassName)} />
      </div>
    )
  }

  return (
    <Image
      src={src!}
      alt={alt}
      className={cn(className)}
      onError={() => setError(true)}
      {...props}
    />
  )
}
