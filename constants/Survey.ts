// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — Enhanced Survey Questions v2
// Comprehensive survey: physical, habits, occupation, lifestyle
// Used to calculate character stats + generate 2.5D avatar
// ═══════════════════════════════════════════════════════════════

import type { StatKey } from './Types';

export interface SurveyQuestion {
  id: string;
  stat: StatKey | 'BODY' | 'LIFESTYLE'; // BODY/LIFESTYLE for character generation
  category: string;
  question: string;
  type: 'slider' | 'choice' | 'number';
  options?: string[];
  min?: number;
  max?: number;
  unit?: string;
  weight: number; // how much this affects the stat (0-1)
}

// ═══ BODY & PHYSICAL PROFILE (for 2.5D character generation) ═══
export const BODY_QUESTIONS: SurveyQuestion[] = [
  {
    id: 'body-height', stat: 'BODY', category: '📏 Thể chất',
    question: 'Chiều cao của bạn (cm)?',
    type: 'number', min: 140, max: 220, unit: 'cm', weight: 1,
  },
  {
    id: 'body-weight', stat: 'BODY', category: '📏 Thể chất',
    question: 'Cân nặng hiện tại (kg)?',
    type: 'number', min: 35, max: 150, unit: 'kg', weight: 1,
  },
  {
    id: 'body-type', stat: 'BODY', category: '📏 Thể chất',
    question: 'Dáng người bạn tự đánh giá?',
    type: 'choice',
    options: ['Gầy / Mảnh khảnh', 'Trung bình', 'Hơi đầy', 'Cơ bắp / Athletic', 'To lớn / Chắc nịch'],
    weight: 1,
  },
  {
    id: 'body-gender', stat: 'BODY', category: '📏 Thể chất',
    question: 'Giới tính?',
    type: 'choice', options: ['Nam', 'Nữ', 'Khác'], weight: 1,
  },
];

// ═══ OCCUPATION & LIFESTYLE (for class suggestion + stats) ═══
export const LIFESTYLE_QUESTIONS: SurveyQuestion[] = [
  {
    id: 'life-job', stat: 'LIFESTYLE', category: '💼 Nghề nghiệp',
    question: 'Nghề nghiệp / lĩnh vực hiện tại?',
    type: 'choice',
    options: ['Học sinh / Sinh viên', 'IT / Công nghệ', 'Y tế / Sức khỏe', 'Kinh doanh / Quản lý', 'Sáng tạo / Nghệ thuật', 'Giáo dục / Nghiên cứu', 'Kỹ thuật / Công nghiệp', 'Tự do / Freelancer', 'Khác'],
    weight: 0.8,
  },
  {
    id: 'life-work-hours', stat: 'LIFESTYLE', category: '💼 Nghề nghiệp',
    question: 'Số giờ làm việc/học tập mỗi ngày?',
    type: 'choice',
    options: ['< 4 giờ', '4-6 giờ', '6-8 giờ', '8-10 giờ', '> 10 giờ'],
    weight: 0.5,
  },
  {
    id: 'life-screen', stat: 'LIFESTYLE', category: '💼 Nghề nghiệp',
    question: 'Thời gian ngồi trước máy tính/điện thoại mỗi ngày?',
    type: 'choice',
    options: ['< 2 giờ', '2-4 giờ', '4-8 giờ', '8-12 giờ', '> 12 giờ'],
    weight: 0.3,
  },
];

// ═══ FITNESS & EXERCISE (STR, AGI, VIT) ═══
export const FITNESS_QUESTIONS: SurveyQuestion[] = [
  {
    id: 'fit-gym', stat: 'STR', category: '🏋️ Thể lực',
    question: 'Bạn tập gym/thể dục bao nhiêu buổi/tuần?',
    type: 'choice',
    options: ['Không tập (0)', '1-2 buổi', '3-4 buổi', '5-6 buổi', 'Mỗi ngày (7)'],
    weight: 0.8,
  },
  {
    id: 'fit-pushup', stat: 'STR', category: '🏋️ Thể lực',
    question: 'Bạn hít đất được bao nhiêu cái liên tục?',
    type: 'slider', min: 0, max: 100, weight: 0.7,
  },
  {
    id: 'fit-run', stat: 'AGI', category: '🏋️ Thể lực',
    question: 'Bạn chạy bộ được tối đa bao xa (km)?',
    type: 'slider', min: 0, max: 42, unit: 'km', weight: 0.6,
  },
  {
    id: 'fit-sport', stat: 'AGI', category: '🏋️ Thể lực',
    question: 'Bạn chơi thể thao phản xạ nhanh không?',
    type: 'choice',
    options: ['Không bao giờ', 'Thỉnh thoảng', 'Hàng tuần', 'Thường xuyên', 'Thi đấu'],
    weight: 0.7,
  },
  {
    id: 'fit-flex', stat: 'AGI', category: '🏋️ Thể lực',
    question: 'Tốc độ gõ phím (WPM)?',
    type: 'choice',
    options: ['< 30 WPM', '30-50 WPM', '50-70 WPM', '70-100 WPM', '100+ WPM'],
    weight: 0.5,
  },
  {
    id: 'fit-str-self', stat: 'STR', category: '🏋️ Thể lực',
    question: 'Tự đánh giá sức mạnh thể chất (1-10)?',
    type: 'slider', min: 1, max: 10, weight: 0.6,
  },
];

// ═══ HEALTH & VITALITY (VIT) ═══
export const HEALTH_QUESTIONS: SurveyQuestion[] = [
  {
    id: 'hp-sleep', stat: 'VIT', category: '❤️ Sức khỏe',
    question: 'Bạn ngủ trung bình mấy tiếng mỗi đêm?',
    type: 'choice',
    options: ['< 5 tiếng', '5-6 tiếng', '6-7 tiếng', '7-8 tiếng', '8+ tiếng'],
    weight: 0.8,
  },
  {
    id: 'hp-sick', stat: 'VIT', category: '❤️ Sức khỏe',
    question: 'Tần suất bị ốm trong năm qua?',
    type: 'choice',
    options: ['Rất thường (5+)', 'Thường (3-4)', 'Bình thường (1-2)', 'Hiếm khi', 'Chưa bao giờ'],
    weight: 0.7,
  },
  {
    id: 'hp-diet', stat: 'VIT', category: '❤️ Sức khỏe',
    question: 'Chế độ ăn uống của bạn?',
    type: 'choice',
    options: ['Không quan tâm', 'Ăn linh tinh / thức ăn nhanh', 'Bình thường', 'Chú ý dinh dưỡng', 'Rất nghiêm ngặt / healthy'],
    weight: 0.6,
  },
  {
    id: 'hp-water', stat: 'VIT', category: '❤️ Sức khỏe',
    question: 'Bạn uống bao nhiêu nước mỗi ngày?',
    type: 'choice',
    options: ['< 1 lít', '1-1.5 lít', '1.5-2 lít', '2-3 lít', '3+ lít'],
    weight: 0.4,
  },
  {
    id: 'hp-stress', stat: 'VIT', category: '❤️ Sức khỏe',
    question: 'Mức độ stress hiện tại (1-10)?',
    type: 'slider', min: 1, max: 10, weight: 0.5,
  },
  {
    id: 'hp-self', stat: 'VIT', category: '❤️ Sức khỏe',
    question: 'Tự đánh giá sức khỏe tổng thể (1-10)?',
    type: 'slider', min: 1, max: 10, weight: 0.6,
  },
];

// ═══ INTELLIGENCE & LEARNING (INT) ═══
export const INTELLIGENCE_QUESTIONS: SurveyQuestion[] = [
  {
    id: 'int-books', stat: 'INT', category: '🧠 Trí tuệ',
    question: 'Bạn đọc bao nhiêu sách/tháng?',
    type: 'choice',
    options: ['0 cuốn', '1-2 cuốn', '3-4 cuốn', '5-7 cuốn', '8+ cuốn'],
    weight: 0.7,
  },
  {
    id: 'int-edu', stat: 'INT', category: '🧠 Trí tuệ',
    question: 'Trình độ học vấn hiện tại?',
    type: 'choice',
    options: ['Phổ thông', 'Cao đẳng / Đại học (TB)', 'Đại học (Khá)', 'Đại học (Giỏi+)', 'Sau đại học'],
    weight: 0.6,
  },
  {
    id: 'int-lang', stat: 'INT', category: '🧠 Trí tuệ',
    question: 'Trình độ ngoại ngữ?',
    type: 'choice',
    options: ['Không biết', 'Cơ bản (A1-A2)', 'Trung cấp (B1-B2)', 'Cao cấp (C1)', 'Thông thạo (C2+)'],
    weight: 0.5,
  },
  {
    id: 'int-logic', stat: 'INT', category: '🧠 Trí tuệ',
    question: 'Tự đánh giá tư duy logic, giải quyết vấn đề (1-10)?',
    type: 'slider', min: 1, max: 10, weight: 0.7,
  },
];

// ═══ WISDOM & EXPERIENCE (WIS) ═══
export const WISDOM_QUESTIONS: SurveyQuestion[] = [
  {
    id: 'wis-exp', stat: 'WIS', category: '📿 Trải nghiệm',
    question: 'Số năm kinh nghiệm làm việc/chuyên sâu?',
    type: 'choice',
    options: ['< 1 năm', '1-3 năm', '3-5 năm', '5-10 năm', '10+ năm'],
    weight: 0.7,
  },
  {
    id: 'wis-plan', stat: 'WIS', category: '📿 Trải nghiệm',
    question: 'Khả năng quản lý thời gian, lập kế hoạch (1-10)?',
    type: 'slider', min: 1, max: 10, weight: 0.8,
  },
  {
    id: 'wis-decide', stat: 'WIS', category: '📿 Trải nghiệm',
    question: 'Tự đánh giá kỹ năng ra quyết định (1-10)?',
    type: 'slider', min: 1, max: 10, weight: 0.6,
  },
  {
    id: 'wis-meditate', stat: 'WIS', category: '📿 Trải nghiệm',
    question: 'Bạn có thiền / mindfulness không?',
    type: 'choice',
    options: ['Không bao giờ', 'Đã thử', 'Thỉnh thoảng', 'Hàng tuần', 'Hàng ngày'],
    weight: 0.4,
  },
];

// ═══ CHARISMA & SOCIAL (CHA) ═══
export const CHARISMA_QUESTIONS: SurveyQuestion[] = [
  {
    id: 'cha-present', stat: 'CHA', category: '🎭 Giao tiếp',
    question: 'Bạn tự tin thuyết trình trước đám đông?',
    type: 'choice',
    options: ['Rất sợ', 'Hơi lo', 'Bình thường', 'Khá tự tin', 'Rất tự tin'],
    weight: 0.8,
  },
  {
    id: 'cha-friends', stat: 'CHA', category: '🎭 Giao tiếp',
    question: 'Số bạn thân / người tin tưởng?',
    type: 'choice',
    options: ['0-1 người', '2-3 người', '4-5 người', '6-10 người', '10+ người'],
    weight: 0.6,
  },
  {
    id: 'cha-social', stat: 'CHA', category: '🎭 Giao tiếp',
    question: 'Tần suất tham gia hoạt động xã hội?',
    type: 'choice',
    options: ['Rất hiếm', '1-2 lần/tháng', 'Hàng tuần', 'Nhiều lần/tuần', 'Hàng ngày'],
    weight: 0.5,
  },
  {
    id: 'cha-self', stat: 'CHA', category: '🎭 Giao tiếp',
    question: 'Tự đánh giá khả năng ảnh hưởng và lãnh đạo (1-10)?',
    type: 'slider', min: 1, max: 10, weight: 0.7,
  },
];

// ═══ ALL QUESTIONS COMBINED ═══
export const ALL_SURVEY_QUESTIONS: SurveyQuestion[] = [
  ...BODY_QUESTIONS,
  ...LIFESTYLE_QUESTIONS,
  ...FITNESS_QUESTIONS,
  ...HEALTH_QUESTIONS,
  ...INTELLIGENCE_QUESTIONS,
  ...WISDOM_QUESTIONS,
  ...CHARISMA_QUESTIONS,
];

// ═══ SURVEY CATEGORIES (for step-by-step onboarding) ═══
export const SURVEY_CATEGORIES = [
  { id: 'body', title: '📏 Thể chất', subtitle: 'Thông tin cơ bản cơ thể', questions: BODY_QUESTIONS },
  { id: 'lifestyle', title: '💼 Nghề nghiệp', subtitle: 'Công việc & lối sống', questions: LIFESTYLE_QUESTIONS },
  { id: 'fitness', title: '🏋️ Thể lực', subtitle: 'Sức mạnh & sự nhanh nhẹn', questions: FITNESS_QUESTIONS },
  { id: 'health', title: '❤️ Sức khỏe', subtitle: 'Sức sống & dinh dưỡng', questions: HEALTH_QUESTIONS },
  { id: 'intelligence', title: '🧠 Trí tuệ', subtitle: 'Học vấn & tư duy', questions: INTELLIGENCE_QUESTIONS },
  { id: 'wisdom', title: '📿 Trải nghiệm', subtitle: 'Kinh nghiệm & kỹ năng sống', questions: WISDOM_QUESTIONS },
  { id: 'charisma', title: '🎭 Giao tiếp', subtitle: 'Sức ảnh hưởng & xã hội', questions: CHARISMA_QUESTIONS },
];

// ═══ CALCULATE STATS FROM SURVEY ═══
export function calculateStatsFromSurvey(answers: Record<string, number>): Record<StatKey, number> {
  const statSums: Record<StatKey, { total: number; weight: number }> = {
    STR: { total: 0, weight: 0 },
    INT: { total: 0, weight: 0 },
    AGI: { total: 0, weight: 0 },
    VIT: { total: 0, weight: 0 },
    WIS: { total: 0, weight: 0 },
    CHA: { total: 0, weight: 0 },
  };

  for (const q of ALL_SURVEY_QUESTIONS) {
    const val = answers[q.id];
    if (val === undefined || q.stat === 'BODY' || q.stat === 'LIFESTYLE') continue;

    const normalized = q.type === 'choice'
      ? (val / ((q.options?.length ?? 5) - 1)) * 100
      : q.max && q.min
        ? ((val - q.min) / (q.max - q.min)) * 100
        : val * 10;

    statSums[q.stat].total += normalized * q.weight;
    statSums[q.stat].weight += q.weight;
  }

  const result: Record<string, number> = {};
  for (const [stat, data] of Object.entries(statSums)) {
    result[stat] = data.weight > 0 ? Math.round(Math.min(100, Math.max(1, data.total / data.weight))) : 30;
  }

  return result as Record<StatKey, number>;
}

// ═══ DETERMINE CHARACTER BODY TYPE FOR 2.5D AVATAR ═══
export type BodyType = 'slim' | 'average' | 'athletic' | 'muscular' | 'heavy';

export function determineBodyType(height: number, weight: number, bodyTypeChoice: number, fitnessLevel: number): BodyType {
  const bmi = weight / ((height / 100) ** 2);

  if (fitnessLevel >= 3 && bmi >= 22 && bmi <= 27) return 'muscular';
  if (fitnessLevel >= 2 && bmi >= 20 && bmi <= 25) return 'athletic';
  if (bmi < 18.5) return 'slim';
  if (bmi > 28) return 'heavy';
  return 'average';
}

// Character evolves based on progress
export interface CharacterAppearance {
  bodyType: BodyType;
  classStyle: string;
  level: number; // affects gear/glow
  armorTier: 'basic' | 'upgraded' | 'elite' | 'legendary' | 'mythic';
}

export function getArmorTier(level: number): CharacterAppearance['armorTier'] {
  if (level >= 50) return 'mythic';
  if (level >= 35) return 'legendary';
  if (level >= 20) return 'elite';
  if (level >= 10) return 'upgraded';
  return 'basic';
}
