import { ApiError } from "@/services/api";

/**
 * Turns an unknown thrown value into a message suitable for the UI.
 */
export function errorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    const fieldErrors = error.fieldErrors;
    if (fieldErrors) {
      const first = Object.values(fieldErrors)[0];
      if (first) {
        return first;
      }
    }
    return error.message;
  }
  return "Something went wrong. Please try again.";
}
