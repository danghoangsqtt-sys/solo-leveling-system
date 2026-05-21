import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        "secondary-container": "#ffdb3c",
        "on-tertiary-fixed": "#00210c",
        "on-primary-fixed": "#001c39",
        "background": "#121222",
        "tertiary-container": "#00b35b",
        "tertiary": "#40e17e",
        "outline-variant": "#414752",
        "on-surface-variant": "#c0c7d4",
        "error": "#ffb4ab",
        "primary-fixed-dim": "#a4c9ff",
        "on-secondary-fixed-variant": "#544600",
        "primary-fixed": "#d4e3ff",
        "surface-container-highest": "#333345",
        "secondary-fixed-dim": "#e9c400",
        "surface-bright": "#38374a",
        "on-error": "#690005",
        "surface-container-high": "#29283a",
        "on-surface": "#e3e0f8",
        "on-primary-container": "#003463",
        "secondary": "#fff9ef",
        "on-secondary-fixed": "#221b00",
        "surface-dim": "#121222",
        "on-secondary": "#3a3000",
        "on-secondary-container": "#725f00",
        "surface-container-low": "#1a1a2b",
        "on-tertiary-container": "#003c1a",
        "outline": "#8a919e",
        "on-tertiary": "#003919",
        "inverse-primary": "#005fad",
        "on-tertiary-fixed-variant": "#005226",
        "primary-container": "#4a9eff",
        "primary": "#a4c9ff",
        "surface-variant": "#333345",
        "error-container": "#93000a",
        "on-primary-fixed-variant": "#004884",
        "on-primary": "#00315d",
        "surface-container-lowest": "#0c0c1d",
        "on-error-container": "#ffdad6",
        "tertiary-fixed-dim": "#40e17e",
        "surface": "#121222",
        "surface-container": "#1e1e2f",
        "inverse-surface": "#e3e0f8",
        "tertiary-fixed": "#63ff97",
        "on-background": "#e3e0f8",
        "surface-tint": "#a4c9ff",
        "inverse-on-surface": "#2f2f40",
        "secondary-fixed": "#ffe16d"
      },
      borderRadius: {
        "DEFAULT": "0.25rem",
        "lg": "0.5rem",
        "xl": "0.75rem",
        "full": "9999px"
      },
      spacing: {
        "margin-mobile": "16px",
        "unit": "4px",
        "container-gap": "24px",
        "gutter": "16px",
        "margin-desktop": "32px"
      },
      fontFamily: {
        "body-bold": ["Inter", "sans-serif"],
        "label-caps": ["Rajdhani", "sans-serif"],
        "headline-md-mobile": ["Rajdhani", "sans-serif"],
        "body-base": ["Inter", "sans-serif"],
        "display-lg": ["Rajdhani", "sans-serif"],
        "headline-md": ["Rajdhani", "sans-serif"],
        "heading": ["Rajdhani", "sans-serif"],
        "mono": ["monospace"]
      },
      fontSize: {
        "body-bold": ["16px", { lineHeight: "24px", fontWeight: "600" }],
        "label-caps": ["12px", { lineHeight: "16px", letterSpacing: "0.1em", fontWeight: "600" }],
        "headline-md-mobile": ["20px", { lineHeight: "28px", letterSpacing: "0.02em", fontWeight: "700" }],
        "body-base": ["16px", { lineHeight: "24px", fontWeight: "400" }],
        "display-lg": ["48px", { lineHeight: "56px", letterSpacing: "0.05em", fontWeight: "700" }],
        "headline-md": ["24px", { lineHeight: "32px", letterSpacing: "0.02em", fontWeight: "700" }]
      },
      animation: {
        "float": "float 6s ease-in-out infinite",
        "pulse-glow": "pulse-glow 3s infinite",
        "shimmer": "shimmer 2.5s infinite",
      },
      keyframes: {
        float: {
          "0%, 100%": { transform: "translateY(0)" },
          "50%": { transform: "translateY(-10px)" }
        },
        "pulse-glow": {
          "0%, 100%": { boxShadow: "0 0 5px rgba(74, 158, 255, 0.2)" },
          "50%": { boxShadow: "0 0 15px rgba(74, 158, 255, 0.6)" }
        },
        shimmer: {
          "0%": { transform: "translateX(-100%) skewX(-15deg)" },
          "100%": { transform: "translateX(200%) skewX(-15deg)" }
        }
      }
    },
  },
  plugins: [],
};
export default config;
