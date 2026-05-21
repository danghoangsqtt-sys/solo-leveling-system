import { createClient } from "@supabase/supabase-js";
import { logger } from "./logger";

// Use environment variables or placeholders if not set
const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL || "https://placeholder-project.supabase.co";
const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || "placeholder-anon-key";

logger.info("Supabase", "Initializing client...", { url: supabaseUrl });
export const supabase = createClient(supabaseUrl, supabaseAnonKey);

/**
 * Utility to fetch the current authenticated user's ID
 */
export async function getCurrentUserId(): Promise<string | null> {
  try {
    const { data: { session }, error } = await supabase.auth.getSession();
    if (error) {
      logger.error("Supabase.Auth", "Failed to get session", error);
      return null;
    }
    if (!session) {
      logger.debug("Supabase.Auth", "No active session found");
      return null;
    }
    logger.info("Supabase.Auth", `User authenticated: ${session.user.id}`);
    return session.user.id;
  } catch (err) {
    logger.error("Supabase.Auth", "Unexpected error during getSession", err);
    return null;
  }
}
