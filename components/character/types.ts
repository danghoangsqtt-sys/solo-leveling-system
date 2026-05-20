// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — CHARACTER TYPES
// ═══════════════════════════════════════════════════════════════

export type BodyType =
  | 'ectomorph' // gầy thanh mảnh
  | 'lean'      // thon gọn
  | 'athletic'  // cân đối thể thao
  | 'stocky'    // đậm người chắc khỏe
  | 'heavy'     // to béo
  | 'large';    // rất to

export interface CharacterStatInput {
  heightCm: number;
  weightKg: number;
  STR: number;
  VIT: number;
  AGI: number;
  level: number;
}

export interface BodyParams {
  shoulderWidth: number;
  armThickness: number;
  chestWidth: number;
  waistWidth: number;
  hipWidth: number;
  thighThickness: number;
  legThickness: number;
  headScale: number;
  torsoHeight: number;
  legHeight: number;
  bodyType: BodyType;
  bmi: number;
}
