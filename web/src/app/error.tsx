"use client";

import { useEffect } from "react";
import { logger } from "@/lib/logger";

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    // Log the error to our internal logger
    logger.error("App", "Global Error Caught", {
      message: error.message,
      stack: error.stack,
      digest: error.digest,
    });
  }, [error]);

  return (
    <html>
      <body>
        <div className="flex flex-col items-center justify-center min-h-screen bg-[#121222] text-[#e3e0f8] p-4 text-center">
          <div className="game-card border border-red-500/50 bg-red-950/20 max-w-lg p-8 rounded-xl backdrop-blur-xl">
            <span className="text-4xl mb-4 block">⚠️</span>
            <h2 className="text-2xl font-bold text-red-400 mb-2 font-heading">LỖI HỆ THỐNG (SYSTEM ERROR)</h2>
            <p className="text-sm text-gray-400 mb-6 font-mono break-words">
              {error.message || "An unexpected error occurred in the Matrix."}
            </p>
            <button
              onClick={() => reset()}
              className="px-6 py-2 bg-red-500/10 border border-red-500/30 text-red-400 rounded-full hover:bg-red-500/20 transition-all font-heading font-bold tracking-widest"
            >
              KHÔI PHỤC HỆ THỐNG
            </button>
          </div>
        </div>
      </body>
    </html>
  );
}
