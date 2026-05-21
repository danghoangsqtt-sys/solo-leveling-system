/**
 * System Logger for Solo Leveling App
 * Provides structured event logging for debugging and system control.
 */

type LogLevel = "INFO" | "WARN" | "ERROR" | "DEBUG";

interface LogEvent {
  level: LogLevel;
  module: string;
  message: string;
  data?: unknown;
  timestamp: string;
}

class Logger {
  private log(level: LogLevel, module: string, message: string, data?: unknown) {
    const event: LogEvent = {
      level,
      module,
      message,
      data,
      timestamp: new Date().toISOString(),
    };

    // Format output
    const logString = `[${event.timestamp}] [${level}] [${module}]: ${message}`;

    switch (level) {
      case "INFO":
        console.log(`%c${logString}`, "color: #4a9eff", data ? data : "");
        break;
      case "WARN":
        console.warn(`%c${logString}`, "color: #ffd700", data ? data : "");
        break;
      case "ERROR":
        console.error(`%c${logString}`, "color: #ff4757; font-weight: bold", data ? data : "");
        // TODO: In production, send this to Sentry or Supabase logs table
        break;
      case "DEBUG":
        if (process.env.NODE_ENV === "development") {
          console.debug(`%c${logString}`, "color: #a4b0be", data ? data : "");
        }
        break;
    }
  }

  info(module: string, message: string, data?: unknown) {
    this.log("INFO", module, message, data);
  }

  warn(module: string, message: string, data?: unknown) {
    this.log("WARN", module, message, data);
  }

  error(module: string, message: string, data?: unknown) {
    this.log("ERROR", module, message, data);
  }

  debug(module: string, message: string, data?: unknown) {
    this.log("DEBUG", module, message, data);
  }
}

export const logger = new Logger();
