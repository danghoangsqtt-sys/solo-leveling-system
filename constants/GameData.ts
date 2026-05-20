// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — Game Data Constants
// Static game data: Classes, Survey Questions, Default Goals/Titles
// ═══════════════════════════════════════════════════════════════

import { ClassInfo, SurveyQuestion, Goal, Title } from './Types';
import { ClassColors } from './Colors';

// ═══ CHARACTER CLASSES ═══
export const CLASSES: ClassInfo[] = [
  {
    id: 'warrior',
    name: 'Warrior',
    nameVi: 'Chiến Binh',
    icon: '🗡️',
    description: 'Kẻ chinh phục giới hạn thể chất. Sức mạnh là quyền năng tối thượng.',
    primaryStats: ['STR', 'VIT'],
    color: ClassColors.warrior,
  },
  {
    id: 'mage',
    name: 'Mage',
    nameVi: 'Pháp Sư',
    icon: '🧙',
    description: 'Tri thức là phép thuật mạnh nhất. Kẻ thấu hiểu vạn vật.',
    primaryStats: ['INT', 'WIS'],
    color: ClassColors.mage,
  },
  {
    id: 'ranger',
    name: 'Ranger',
    nameVi: 'Xạ Thủ',
    icon: '🏹',
    description: 'Nhanh, chính xác, không bao giờ bỏ lỡ mục tiêu. Kẻ tự do.',
    primaryStats: ['AGI', 'INT'],
    color: ClassColors.ranger,
  },
  {
    id: 'guardian',
    name: 'Guardian',
    nameVi: 'Hộ Vệ',
    icon: '🛡️',
    description: 'Bất khuất, kiên cường. Là tường thành bảo vệ mọi người.',
    primaryStats: ['VIT', 'WIS'],
    color: ClassColors.guardian,
  },
  {
    id: 'bard',
    name: 'Bard',
    nameVi: 'Nghệ Sĩ',
    icon: '🎭',
    description: 'Sáng tạo là vũ khí, sức hút là sức mạnh. Kẻ truyền cảm hứng.',
    primaryStats: ['CHA', 'AGI'],
    color: ClassColors.bard,
  },
  {
    id: 'alchemist',
    name: 'Alchemist',
    nameVi: 'Giả Kim',
    icon: '⚗️',
    description: 'Biến ý tưởng thành hiện thực. Kẻ sáng tạo từ hư vô.',
    primaryStats: ['INT', 'AGI'],
    color: ClassColors.alchemist,
  },
  {
    id: 'healer',
    name: 'Healer',
    nameVi: 'Thầy Chữa',
    icon: '🌿',
    description: 'Chữa lành thân thể và tâm hồn. Kẻ mang ánh sáng cho người khác.',
    primaryStats: ['WIS', 'CHA'],
    color: ClassColors.healer,
  },
  {
    id: 'scholar',
    name: 'Scholar',
    nameVi: 'Học Giả',
    icon: '📜',
    description: 'Tri thức không giới hạn. Kẻ truyền đạt sự hiểu biết cho thế gian.',
    primaryStats: ['INT', 'WIS'],
    color: ClassColors.scholar,
  },
];

// ═══ SURVEY QUESTIONS (3-4 per stat = 18-24 total) ═══
export const SURVEY_QUESTIONS: SurveyQuestion[] = [
  // STR - Strength
  {
    id: 'str-1',
    stat: 'STR',
    question: 'Bạn tập thể dục/gym bao nhiêu buổi mỗi tuần?',
    type: 'multiple_choice',
    options: ['Không tập (0)', '1-2 buổi', '3-4 buổi', '5-6 buổi', 'Mỗi ngày (7)'],
  },
  {
    id: 'str-2',
    stat: 'STR',
    question: 'Bạn có thể hít đất bao nhiêu cái liên tục?',
    type: 'slider',
    min: 0,
    max: 100,
  },
  {
    id: 'str-3',
    stat: 'STR',
    question: 'Tự đánh giá sức mạnh thể chất của bạn?',
    type: 'slider',
    min: 1,
    max: 10,
  },

  // INT - Intelligence
  {
    id: 'int-1',
    stat: 'INT',
    question: 'Bạn đọc bao nhiêu cuốn sách mỗi tháng?',
    type: 'multiple_choice',
    options: ['0 cuốn', '1-2 cuốn', '3-4 cuốn', '5-7 cuốn', '8+ cuốn'],
  },
  {
    id: 'int-2',
    stat: 'INT',
    question: 'Trình độ học vấn / GPA hiện tại?',
    type: 'multiple_choice',
    options: ['Đang học phổ thông', 'Cao đẳng/Đại học (TB)', 'Đại học (Khá)', 'Đại học (Giỏi+)', 'Sau đại học'],
  },
  {
    id: 'int-3',
    stat: 'INT',
    question: 'Tự đánh giá khả năng tư duy logic, giải quyết vấn đề?',
    type: 'slider',
    min: 1,
    max: 10,
  },

  // AGI - Agility
  {
    id: 'agi-1',
    stat: 'AGI',
    question: 'Tốc độ gõ phím của bạn (WPM)?',
    type: 'multiple_choice',
    options: ['<30 WPM', '30-50 WPM', '50-70 WPM', '70-100 WPM', '100+ WPM'],
  },
  {
    id: 'agi-2',
    stat: 'AGI',
    question: 'Bạn có chơi thể thao đòi hỏi phản xạ nhanh không?',
    type: 'multiple_choice',
    options: ['Không bao giờ', 'Thỉnh thoảng', 'Hàng tuần', 'Thường xuyên', 'Chuyên nghiệp'],
  },
  {
    id: 'agi-3',
    stat: 'AGI',
    question: 'Tự đánh giá sự nhanh nhẹn và phản xạ?',
    type: 'slider',
    min: 1,
    max: 10,
  },

  // VIT - Vitality
  {
    id: 'vit-1',
    stat: 'VIT',
    question: 'Bạn ngủ trung bình bao nhiêu tiếng mỗi đêm?',
    type: 'multiple_choice',
    options: ['<5 tiếng', '5-6 tiếng', '6-7 tiếng', '7-8 tiếng', '8+ tiếng'],
  },
  {
    id: 'vit-2',
    stat: 'VIT',
    question: 'Tần suất bạn bị ốm trong năm qua?',
    type: 'multiple_choice',
    options: ['Rất thường xuyên (5+)', 'Thường (3-4 lần)', 'Bình thường (1-2 lần)', 'Hiếm khi', 'Chưa bao giờ'],
  },
  {
    id: 'vit-3',
    stat: 'VIT',
    question: 'Tự đánh giá sức khỏe tổng thể của bạn?',
    type: 'slider',
    min: 1,
    max: 10,
  },

  // WIS - Wisdom
  {
    id: 'wis-1',
    stat: 'WIS',
    question: 'Số năm kinh nghiệm làm việc / học tập chuyên sâu?',
    type: 'multiple_choice',
    options: ['<1 năm', '1-3 năm', '3-5 năm', '5-10 năm', '10+ năm'],
  },
  {
    id: 'wis-2',
    stat: 'WIS',
    question: 'Khả năng tự quản lý thời gian và kế hoạch?',
    type: 'slider',
    min: 1,
    max: 10,
  },
  {
    id: 'wis-3',
    stat: 'WIS',
    question: 'Tự đánh giá khả năng ra quyết định và kỹ năng sống?',
    type: 'slider',
    min: 1,
    max: 10,
  },

  // CHA - Charisma
  {
    id: 'cha-1',
    stat: 'CHA',
    question: 'Bạn tự tin thuyết trình trước đám đông không?',
    type: 'multiple_choice',
    options: ['Rất sợ', 'Hơi lo lắng', 'Bình thường', 'Khá tự tin', 'Rất tự tin'],
  },
  {
    id: 'cha-2',
    stat: 'CHA',
    question: 'Số bạn thân / người tin tưởng?',
    type: 'multiple_choice',
    options: ['0-1 người', '2-3 người', '4-5 người', '6-10 người', '10+ người'],
  },
  {
    id: 'cha-3',
    stat: 'CHA',
    question: 'Tự đánh giá khả năng giao tiếp và ảnh hưởng?',
    type: 'slider',
    min: 1,
    max: 10,
  },
];

// ═══ DEFAULT GOALS ═══
export const DEFAULT_GOALS: Omit<Goal, 'id' | 'createdAt'>[] = [
  { name: 'Body Transformation', description: 'Tăng cơ giảm mỡ', category: 'fitness', icon: '🏋️', deadline: null, isActive: true },
  { name: 'Intelligence Boost', description: 'Nâng cao trí tuệ', category: 'learning', icon: '🧠', deadline: null, isActive: true },
  { name: 'Language Mastery', description: 'Đạt IELTS 7.0', category: 'language', icon: '📚', deadline: null, isActive: true },
  { name: 'Tech Skill: IoT', description: 'Thành thạo lập trình nhúng IoT', category: 'tech', icon: '💻', deadline: null, isActive: true },
  { name: 'Digital Art', description: 'Học vẽ digital art', category: 'creative', icon: '🎨', deadline: null, isActive: true },
  { name: 'Financial Freedom', description: 'Xây dựng thu nhập thụ động', category: 'finance', icon: '💰', deadline: null, isActive: true },
  { name: 'Mind & Soul', description: 'Cải thiện sức khỏe tinh thần', category: 'wellness', icon: '🧘', deadline: null, isActive: true },
  { name: 'Academic Excellence', description: 'Ôn thi đại học', category: 'academic', icon: '📐', deadline: null, isActive: true },
  { name: 'Music Skill', description: 'Học chơi guitar', category: 'creative', icon: '🎸', deadline: null, isActive: true },
  { name: 'Communication Mastery', description: 'Nâng cao kỹ năng giao tiếp', category: 'social', icon: '🗣️', deadline: null, isActive: true },
];

// ═══ EXP TABLE ═══
export function getExpToNextLevel(level: number): number {
  // Base 1000, increases 15% each level
  return Math.floor(1000 * Math.pow(1.15, level - 1));
}

export function getLevelFromTotalExp(totalExp: number): { level: number; currentExp: number; expToNext: number } {
  let level = 1;
  let remaining = totalExp;
  while (remaining >= getExpToNextLevel(level)) {
    remaining -= getExpToNextLevel(level);
    level++;
  }
  return {
    level,
    currentExp: remaining,
    expToNext: getExpToNextLevel(level),
  };
}

// ═══ LEVEL THEME ═══
export function getLevelThemeName(level: number): string {
  if (level <= 10) return 'forest';
  if (level <= 20) return 'desert';
  if (level <= 30) return 'ocean';
  if (level <= 40) return 'mountain';
  if (level <= 50) return 'castle';
  return 'galaxy';
}
