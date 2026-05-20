// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — USE BODY PARAMS HOOK
// Calculates dynamic proportions based on real stats
// ═══════════════════════════════════════════════════════════════

import { useMemo } from 'react';
import type { CharacterStatInput, BodyParams, BodyType } from './types';

export function useBodyParams(input: CharacterStatInput): BodyParams {
  return useMemo(() => {
    const { heightCm, weightKg, STR, VIT, AGI } = input;

    // Normalizing values
    const hMeters = heightCm / 100;
    const bmi = hMeters > 0 ? weightKg / (hMeters * hMeters) : 22;

    // Determine Body Type
    let bodyType: BodyType = 'lean';
    let bmiFactor = 1.0;

    if (bmi < 18.5) {
      bodyType = 'ectomorph';
      bmiFactor = 0.85;
    } else if (bmi >= 18.5 && bmi < 23) {
      bodyType = 'lean';
      bmiFactor = 1.0;
    } else if (bmi >= 23 && bmi < 25) {
      bodyType = 'athletic';
      bmiFactor = 1.05;
    } else if (bmi >= 25 && bmi < 30) {
      bodyType = 'stocky';
      bmiFactor = 1.18;
    } else if (bmi >= 30 && bmi < 35) {
      bodyType = 'heavy';
      bmiFactor = 1.3;
    } else {
      bodyType = 'large';
      bmiFactor = 1.45;
    }

    // Height ratio (base 170cm)
    const heightRatio = Math.max(0.8, Math.min(1.2, heightCm / 170));

    // Base dimensions before modification
    const baseShoulder = 50;
    const baseArm = 8;
    const baseChest = 46;
    const baseWaist = 36;
    const baseHip = 40;
    const baseThigh = 10;
    const baseLeg = 7;
    const baseHead = 12;

    // Modifiers based on Stats
    const strFactor = STR / 100; // 0 to 1
    const vitFactor = VIT / 100;
    const agiFactor = AGI / 100;

    // Formulas for body scaling
    const shoulderWidth = baseShoulder * bmiFactor * (1.0 + strFactor * 0.25);
    const armThickness = baseArm * bmiFactor * (1.0 + strFactor * 0.4);
    const chestWidth = baseChest * bmiFactor * (1.0 + strFactor * 0.2 + vitFactor * 0.1);
    
    // AGI makes waist thinner, VIT/BMI makes it thicker
    const waistWidth = baseWaist * bmiFactor * (1.0 + vitFactor * 0.15 - agiFactor * 0.12);
    const hipWidth = baseHip * bmiFactor * (1.0 + vitFactor * 0.1);
    
    const thighThickness = baseThigh * bmiFactor * (1.0 + strFactor * 0.15);
    const legThickness = baseLeg * bmiFactor * (1.0 + strFactor * 0.1);
    
    // Head size should scale inverse to height to look heroic, but proportional
    const headScale = baseHead * (1.0 - (heightRatio - 1.0) * 0.25);

    // Torso and Leg height relative dimensions
    const torsoHeight = 90 * heightRatio * (1.0 - agiFactor * 0.05);
    const legHeight = 110 * heightRatio * (1.0 + agiFactor * 0.08);

    return {
      shoulderWidth,
      armThickness,
      chestWidth,
      waistWidth,
      hipWidth,
      thighThickness,
      legThickness,
      headScale,
      torsoHeight,
      legHeight,
      bodyType,
      bmi,
    };
  }, [input]);
}
