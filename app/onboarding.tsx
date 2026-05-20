// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — ONBOARDING SURVEY SCREEN
// Step-by-step character creation through comprehensive survey
// Collects: body info, lifestyle, fitness, health, intellect, etc.
// Saves to GameContext on completion
// ═══════════════════════════════════════════════════════════════

import React, { useState, useRef, useEffect } from 'react';
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  TouchableOpacity,
  TextInput,
  Animated,
  Dimensions,
  Platform,
  Image,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useTheme, Typography, Radius, Spacing } from '@/constants/Theme';
import { SystemColors, StatColors } from '@/constants/Colors';
import {
  SURVEY_CATEGORIES,
  ALL_SURVEY_QUESTIONS,
  calculateStatsFromSurvey,
  determineBodyType,
  getArmorTier,
  type SurveyQuestion,
} from '@/constants/Survey';
import type { StatKey } from '@/constants/Types';
import { useGame } from '@/constants/GameContext';
import { createUserFromOnboarding, generateDailyQuests, getDefaultTitles, getStarterInventory } from '@/constants/DefaultData';

const { width: SW } = Dimensions.get('window');

// ═══ CHOICE OPTION BUTTON ═══
function ChoiceOption({
  label,
  selected,
  index,
  onPress,
}: {
  label: string;
  selected: boolean;
  index: number;
  onPress: () => void;
}) {
  const { theme } = useTheme();
  const scaleAnim = useRef(new Animated.Value(1)).current;

  const handlePress = () => {
    Animated.sequence([
      Animated.timing(scaleAnim, { toValue: 0.95, duration: 80, useNativeDriver: true }),
      Animated.timing(scaleAnim, { toValue: 1, duration: 120, useNativeDriver: true }),
    ]).start();
    onPress();
  };

  return (
    <Animated.View style={{ transform: [{ scale: scaleAnim }] }}>
      <TouchableOpacity
        style={[
          st.choiceBtn,
          {
            backgroundColor: selected ? `${SystemColors.blue}12` : theme.colors.bgCard,
            borderColor: selected ? SystemColors.blue : theme.colors.border,
            borderWidth: selected ? 1.5 : 1,
          },
        ]}
        onPress={handlePress}
        activeOpacity={0.7}>
        <View
          style={[
            st.choiceRadio,
            {
              borderColor: selected ? SystemColors.blue : theme.colors.textMuted,
              backgroundColor: selected ? SystemColors.blue : 'transparent',
            },
          ]}>
          {selected && <View style={st.choiceRadioInner} />}
        </View>
        <Text
          style={[
            st.choiceLabel,
            {
              color: selected ? SystemColors.blue : theme.colors.text,
              fontWeight: selected ? '700' : '500',
            },
          ]}>
          {label}
        </Text>
      </TouchableOpacity>
    </Animated.View>
  );
}

// ═══ SLIDER INPUT ═══
function SliderInput({
  value,
  min,
  max,
  onChange,
  unit,
}: {
  value: number;
  min: number;
  max: number;
  onChange: (v: number) => void;
  unit?: string;
}) {
  const { theme } = useTheme();
  const pct = ((value - min) / (max - min)) * 100;

  return (
    <View style={st.sliderWrap}>
      <View style={st.sliderHeader}>
        <Text style={[st.sliderMin, { color: theme.colors.textMuted }]}>{min}</Text>
        <View style={[st.sliderValBadge, { backgroundColor: `${SystemColors.blue}15`, borderColor: `${SystemColors.blue}30` }]}>
          <Text style={[st.sliderValText, { color: SystemColors.blue }]}>
            {value}{unit ? ` ${unit}` : ''}
          </Text>
        </View>
        <Text style={[st.sliderMax, { color: theme.colors.textMuted }]}>{max}</Text>
      </View>

      {/* Custom slider track */}
      <View style={[st.sliderTrack, { backgroundColor: theme.colors.bgSurface }]}>
        <LinearGradient
          colors={['#3B82F6', '#8B5CF6']}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 0 }}
          style={[st.sliderFill, { width: `${pct}%` }]}
        />
      </View>

      {/* +/- buttons for fine control */}
      <View style={st.sliderControls}>
        <TouchableOpacity
          style={[st.sliderBtn, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}
          onPress={() => onChange(Math.max(min, value - 1))}>
          <Text style={[st.sliderBtnText, { color: theme.colors.text }]}>−</Text>
        </TouchableOpacity>

        {/* Quick step buttons */}
        {Array.from({ length: 5 }, (_, i) => {
          const step = min + Math.round(((max - min) * (i + 1)) / 6);
          const isActive = Math.abs(value - step) < (max - min) * 0.08;
          return (
            <TouchableOpacity
              key={i}
              style={[
                st.sliderStep,
                {
                  backgroundColor: isActive ? `${SystemColors.blue}15` : theme.colors.bgCard,
                  borderColor: isActive ? SystemColors.blue : theme.colors.border,
                },
              ]}
              onPress={() => onChange(step)}>
              <Text style={[st.sliderStepText, { color: isActive ? SystemColors.blue : theme.colors.textMuted }]}>
                {step}
              </Text>
            </TouchableOpacity>
          );
        })}

        <TouchableOpacity
          style={[st.sliderBtn, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}
          onPress={() => onChange(Math.min(max, value + 1))}>
          <Text style={[st.sliderBtnText, { color: theme.colors.text }]}>+</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

// ═══ NUMBER INPUT ═══
function NumberInput({
  value,
  min,
  max,
  unit,
  onChange,
}: {
  value: number;
  min: number;
  max: number;
  unit?: string;
  onChange: (v: number) => void;
}) {
  const { theme } = useTheme();
  const [textVal, setTextVal] = useState(value > 0 ? String(value) : '');

  return (
    <View style={st.numberWrap}>
      <TouchableOpacity
        style={[st.numBtn, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}
        onPress={() => { const v = Math.max(min, value - 1); onChange(v); setTextVal(String(v)); }}>
        <Text style={[st.numBtnText, { color: theme.colors.text }]}>−</Text>
      </TouchableOpacity>

      <View style={[st.numInputWrap, { backgroundColor: theme.colors.bgCard, borderColor: `${SystemColors.blue}30` }]}>
        <TextInput
          style={[st.numInput, { color: theme.colors.text }]}
          value={textVal}
          onChangeText={(t) => {
            setTextVal(t);
            const n = parseInt(t, 10);
            if (!isNaN(n) && n >= min && n <= max) onChange(n);
          }}
          keyboardType="numeric"
          placeholder="..."
          placeholderTextColor={theme.colors.textMuted}
        />
        {unit && <Text style={[st.numUnit, { color: theme.colors.textMuted }]}>{unit}</Text>}
      </View>

      <TouchableOpacity
        style={[st.numBtn, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}
        onPress={() => { const v = Math.min(max, value + 1); onChange(v); setTextVal(String(v)); }}>
        <Text style={[st.numBtnText, { color: theme.colors.text }]}>+</Text>
      </TouchableOpacity>
    </View>
  );
}

// ═══ QUESTION CARD ═══
function QuestionCard({ q, answer, onAnswer }: { q: SurveyQuestion; answer?: number; onAnswer: (v: number) => void }) {
  const { theme } = useTheme();

  return (
    <View style={[st.qCard, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
      <Text style={[st.qText, { color: theme.colors.text }]}>{q.question}</Text>

      {q.type === 'choice' && q.options && (
        <View style={st.choiceList}>
          {q.options.map((opt, i) => (
            <ChoiceOption
              key={i}
              label={opt}
              selected={answer === i}
              index={i}
              onPress={() => onAnswer(i)}
            />
          ))}
        </View>
      )}

      {q.type === 'slider' && (
        <SliderInput
          value={answer ?? q.min ?? 1}
          min={q.min ?? 1}
          max={q.max ?? 10}
          unit={q.unit}
          onChange={onAnswer}
        />
      )}

      {q.type === 'number' && (
        <NumberInput
          value={answer ?? 0}
          min={q.min ?? 0}
          max={q.max ?? 999}
          unit={q.unit}
          onChange={onAnswer}
        />
      )}
    </View>
  );
}

// ═══ STAT RESULT BAR ═══
function StatResult({ stat, value }: { stat: StatKey; value: number }) {
  const { theme } = useTheme();
  const anim = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    Animated.timing(anim, { toValue: value / 100, duration: 1200, useNativeDriver: false }).start();
  }, [value]);

  const w = anim.interpolate({ inputRange: [0, 1], outputRange: ['0%', '100%'] });

  return (
    <View style={st.resultRow}>
      <Text style={[st.resultLabel, { color: StatColors[stat] }]}>{stat}</Text>
      <View style={[st.resultBarBg, { backgroundColor: theme.colors.bgSurface }]}>
        <Animated.View style={[st.resultBarFill, { width: w, backgroundColor: StatColors[stat] }]} />
      </View>
      <Text style={[st.resultVal, { color: theme.colors.text }]}>{value}</Text>
    </View>
  );
}

// ═══ MAIN ONBOARDING SCREEN ═══
export default function OnboardingScreen() {
  const { theme, isDark } = useTheme();
  const { dispatch } = useGame();
  const [step, setStep] = useState(0); // 0=welcome, 1-7=categories, 8=name, 9=results
  const [answers, setAnswers] = useState<Record<string, number>>({});
  const [nickname, setNickname] = useState('');
  const fadeAnim = useRef(new Animated.Value(1)).current;
  const scrollRef = useRef<ScrollView>(null);

  const totalSteps = SURVEY_CATEGORIES.length + 2; // +welcome +results
  const progress = step / totalSteps;

  const setAnswer = (qId: string, val: number) => {
    setAnswers(prev => ({ ...prev, [qId]: val }));
  };

  const animateTransition = (next: number) => {
    Animated.sequence([
      Animated.timing(fadeAnim, { toValue: 0.3, duration: 150, useNativeDriver: true }),
      Animated.timing(fadeAnim, { toValue: 1, duration: 250, useNativeDriver: true }),
    ]).start();
    setTimeout(() => {
      setStep(next);
      scrollRef.current?.scrollTo({ y: 0, animated: false });
    }, 150);
  };

  const currentCategory = step >= 1 && step <= SURVEY_CATEGORIES.length
    ? SURVEY_CATEGORIES[step - 1]
    : null;

  const canProceed = () => {
    if (step === 0) return true;
    if (step === totalSteps - 1) return nickname.trim().length > 0;
    if (currentCategory) {
      const answered = currentCategory.questions.filter(q => answers[q.id] !== undefined).length;
      return answered >= Math.ceil(currentCategory.questions.length * 0.5); // At least 50% answered
    }
    return true;
  };

  const computedStats = step >= totalSteps
    ? calculateStatsFromSurvey(answers)
    : null;

  // ═══ HANDLE COMPLETION — Save to GameContext ═══
  const handleComplete = () => {
    if (!computedStats) return;

    // Create user profile
    const user = createUserFromOnboarding(nickname.trim(), computedStats);

    // Set body info if available
    if (answers['body-height']) user.heightCm = answers['body-height'];
    if (answers['body-weight']) user.weightKg = answers['body-weight'];
    if (answers['body-gender'] !== undefined) {
      user.gender = answers['body-gender'] === 0 ? 'male' : answers['body-gender'] === 1 ? 'female' : 'other';
    }

    // Dispatch all initial data
    dispatch({ type: 'SET_USER', payload: user });
    dispatch({ type: 'SET_QUESTS', payload: generateDailyQuests() });
    dispatch({ type: 'SET_TITLES', payload: getDefaultTitles() });
    dispatch({ type: 'SET_INVENTORY', payload: getStarterInventory() });
    dispatch({ type: 'SET_ONBOARDED', payload: true });

    // Navigate to main app
    router.replace('/(tabs)');
  };

  return (
    <View style={[st.container, { backgroundColor: theme.colors.bg }]}>
      {/* Progress bar */}
      <View style={st.progressWrap}>
        <View style={[st.progressBg, { backgroundColor: theme.colors.bgSurface }]}>
          <LinearGradient
            colors={['#3B82F6', '#8B5CF6']}
            start={{ x: 0, y: 0 }}
            end={{ x: 1, y: 0 }}
            style={[st.progressFill, { width: `${progress * 100}%` }]}
          />
        </View>
        <Text style={[st.progressText, { color: theme.colors.textMuted }]}>
          {step}/{totalSteps}
        </Text>
      </View>

      <Animated.View style={{ flex: 1, opacity: fadeAnim }}>
        <ScrollView
          ref={scrollRef}
          contentContainerStyle={st.scroll}
          showsVerticalScrollIndicator={false}>

          {/* ═══ STEP 0: WELCOME ═══ */}
          {step === 0 && (
            <View style={st.welcomeWrap}>
              <LinearGradient
                colors={isDark ? ['#1E293B', '#0F172A'] : ['#DBEAFE', '#EDE9FE']}
                style={st.welcomeHero}>
                <Text style={st.welcomeEmoji}>⚔️</Text>
                <Text style={[st.welcomeTitle, { color: theme.colors.text }]}>
                  THE SYSTEM AWAKENS
                </Text>
                <Text style={[st.welcomeSubtitle, { color: SystemColors.blue }]}>
                  Hệ Thống Thức Tỉnh
                </Text>
                <Text style={[st.welcomeDesc, { color: theme.colors.textSecondary }]}>
                  Hệ thống sẽ quét và phân tích năng lực hiện tại của bạn.{'\n'}
                  Trả lời trung thực để nhận chỉ số chính xác nhất.
                </Text>
              </LinearGradient>

              <View style={[st.welcomeInfo, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
                <Text style={[st.welcomeInfoTitle, { color: theme.colors.text }]}>📋 Khảo sát bao gồm:</Text>
                {SURVEY_CATEGORIES.map((cat, i) => (
                  <View key={cat.id} style={st.welcomeItem}>
                    <Text style={[st.welcomeItemNum, { color: SystemColors.blue }]}>{i + 1}</Text>
                    <View>
                      <Text style={[st.welcomeItemTitle, { color: theme.colors.text }]}>{cat.title}</Text>
                      <Text style={[st.welcomeItemSub, { color: theme.colors.textMuted }]}>{cat.subtitle}</Text>
                    </View>
                  </View>
                ))}
              </View>

              <View style={[st.welcomeNote, { backgroundColor: `${SystemColors.blue}08`, borderColor: `${SystemColors.blue}20` }]}>
                <Text style={[st.welcomeNoteText, { color: theme.colors.textSecondary }]}>
                  ⏱ Thời gian: ~5 phút  •  📊 {ALL_SURVEY_QUESTIONS.length} câu hỏi  •  🔒 Dữ liệu lưu local
                </Text>
              </View>
            </View>
          )}

          {/* ═══ STEPS 1-7: SURVEY CATEGORIES ═══ */}
          {currentCategory && (
            <View>
              <View style={st.catHeader}>
                <Text style={[st.catTitle, { color: theme.colors.text }]}>{currentCategory.title}</Text>
                <Text style={[st.catSub, { color: theme.colors.textMuted }]}>{currentCategory.subtitle}</Text>
                <Text style={[st.catCount, { color: SystemColors.blue }]}>
                  {currentCategory.questions.filter(q => answers[q.id] !== undefined).length}/{currentCategory.questions.length} đã trả lời
                </Text>
              </View>

              {currentCategory.questions.map(q => (
                <QuestionCard
                  key={q.id}
                  q={q}
                  answer={answers[q.id]}
                  onAnswer={(v) => setAnswer(q.id, v)}
                />
              ))}
            </View>
          )}

          {/* ═══ STEP 8: NICKNAME ═══ */}
          {step === totalSteps - 1 && (
            <View style={st.nameWrap}>
              <LinearGradient
                colors={isDark ? ['#1E293B', '#0F172A'] : ['#DBEAFE', '#EDE9FE']}
                style={st.nameHero}>
                <Text style={st.nameEmoji}>🗡️</Text>
                <Text style={[st.nameTitle, { color: theme.colors.text }]}>
                  Đặt tên Hunter
                </Text>
                <Text style={[st.nameDesc, { color: theme.colors.textSecondary }]}>
                  Chọn một biệt danh cho nhân vật của bạn
                </Text>
              </LinearGradient>

              <View style={[st.nameInputCard, { backgroundColor: theme.colors.bgCard, borderColor: `${SystemColors.blue}30` }]}>
                <TextInput
                  style={[st.nameInput, { color: theme.colors.text, borderColor: theme.colors.border }]}
                  value={nickname}
                  onChangeText={setNickname}
                  placeholder="Shadow Monarch..."
                  placeholderTextColor={theme.colors.textMuted}
                  maxLength={20}
                  autoFocus
                />
                <Text style={[st.nameCount, { color: theme.colors.textMuted }]}>
                  {nickname.length}/20
                </Text>
              </View>

              {/* Preview character */}
              <View style={st.previewWrap}>
                <Image
                  source={require('@/assets/images/characters/character_default.png')}
                  style={st.previewImg}
                  resizeMode="contain"
                />
                {nickname.trim() && (
                  <Text style={[st.previewName, { color: SystemColors.blue }]}>
                    {nickname}
                  </Text>
                )}
              </View>
            </View>
          )}

          {/* ═══ STEP 9: RESULTS ═══ */}
          {step === totalSteps && computedStats && (
            <View style={st.resultsWrap}>
              <LinearGradient
                colors={isDark ? ['#1E293B', '#0F172A'] : ['#D1FAE5', '#DBEAFE', '#EDE9FE']}
                style={st.resultsHero}>
                <Text style={st.resultsEmoji}>🎉</Text>
                <Text style={[st.resultsTitle, { color: theme.colors.text }]}>
                  SYSTEM SCAN COMPLETE
                </Text>
                <Text style={[st.resultsSub, { color: SystemColors.green }]}>
                  Hunter {nickname} — Đã được đánh giá!
                </Text>
              </LinearGradient>

              {/* Character preview */}
              <View style={st.resultsChar}>
                <Image
                  source={require('@/assets/images/characters/character_default.png')}
                  style={st.resultsCharImg}
                  resizeMode="contain"
                />
              </View>

              {/* Stats */}
              <View style={[st.resultsCard, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
                <Text style={[st.resultsCardTitle, { color: theme.colors.text }]}>📊 Chỉ số khởi đầu</Text>
                {(Object.keys(computedStats) as StatKey[]).map(stat => (
                  <StatResult key={stat} stat={stat} value={computedStats[stat]} />
                ))}
              </View>

              {/* Body info */}
              <View style={[st.resultsCard, { backgroundColor: theme.colors.bgCard, borderColor: theme.colors.border }]}>
                <Text style={[st.resultsCardTitle, { color: theme.colors.text }]}>🧬 Thông tin cơ thể</Text>
                <View style={st.bodyInfoGrid}>
                  {answers['body-height'] && (
                    <View style={st.bodyInfoItem}>
                      <Text style={[st.bodyInfoVal, { color: SystemColors.blue }]}>{answers['body-height']} cm</Text>
                      <Text style={[st.bodyInfoLbl, { color: theme.colors.textMuted }]}>Chiều cao</Text>
                    </View>
                  )}
                  {answers['body-weight'] && (
                    <View style={st.bodyInfoItem}>
                      <Text style={[st.bodyInfoVal, { color: SystemColors.orange }]}>{answers['body-weight']} kg</Text>
                      <Text style={[st.bodyInfoLbl, { color: theme.colors.textMuted }]}>Cân nặng</Text>
                    </View>
                  )}
                  {answers['body-height'] && answers['body-weight'] && (
                    <View style={st.bodyInfoItem}>
                      <Text style={[st.bodyInfoVal, { color: SystemColors.green }]}>
                        {(answers['body-weight'] / ((answers['body-height'] / 100) ** 2)).toFixed(1)}
                      </Text>
                      <Text style={[st.bodyInfoLbl, { color: theme.colors.textMuted }]}>BMI</Text>
                    </View>
                  )}
                </View>
              </View>

              {/* Starter rewards */}
              <View style={[st.resultsCard, { backgroundColor: theme.colors.bgCard, borderColor: `${SystemColors.gold}30` }]}>
                <Text style={[st.resultsCardTitle, { color: theme.colors.text }]}>🎁 Phần thưởng khởi đầu</Text>
                <View style={st.rewardsList}>
                  <Text style={[st.rewardItem, { color: SystemColors.gold }]}>💰 100 Gold</Text>
                  <Text style={[st.rewardItem, { color: SystemColors.cyan }]}>💎 5 Gems</Text>
                  <Text style={[st.rewardItem, { color: SystemColors.green }]}>🌱 Title: Tân Binh</Text>
                  <Text style={[st.rewardItem, { color: SystemColors.purple }]}>🧪 3x EXP Potion</Text>
                  <Text style={[st.rewardItem, { color: SystemColors.blue }]}>📋 8 Daily Quests</Text>
                </View>
              </View>
            </View>
          )}

          <View style={{ height: 100 }} />
        </ScrollView>
      </Animated.View>

      {/* ═══ BOTTOM NAVIGATION ═══ */}
      <View style={[st.bottomBar, { backgroundColor: theme.colors.bgCard, borderTopColor: theme.colors.border }]}>
        {step > 0 && step <= totalSteps - 1 && (
          <TouchableOpacity
            style={[st.backBtn, { borderColor: theme.colors.border }]}
            onPress={() => animateTransition(step - 1)}>
            <Text style={[st.backBtnText, { color: theme.colors.textSecondary }]}>← Quay lại</Text>
          </TouchableOpacity>
        )}

        {step < totalSteps && (
          <TouchableOpacity
            style={[
              st.nextBtn,
              {
                backgroundColor: canProceed() ? SystemColors.blue : theme.colors.bgSurface,
                opacity: canProceed() ? 1 : 0.5,
                flex: step === 0 ? 1 : undefined,
              },
            ]}
            onPress={() => canProceed() && animateTransition(step + 1)}
            disabled={!canProceed()}>
            <Text style={st.nextBtnText}>
              {step === 0 ? '⚔️ Bắt đầu khảo sát' : step === totalSteps - 1 ? '✨ Hoàn thành' : 'Tiếp theo →'}
            </Text>
          </TouchableOpacity>
        )}

        {step === totalSteps && (
          <TouchableOpacity
            style={[st.nextBtn, { backgroundColor: SystemColors.green, flex: 1 }]}
            onPress={handleComplete}>
            <Text style={st.nextBtnText}>🏠 Vào Hệ Thống</Text>
          </TouchableOpacity>
        )}
      </View>
    </View>
  );
}

const st = StyleSheet.create({
  container: { flex: 1 },
  scroll: { paddingHorizontal: 16, paddingTop: 8 },

  // Progress
  progressWrap: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingTop: Platform.OS === 'ios' ? 56 : 40, paddingBottom: 8, gap: 8 },
  progressBg: { flex: 1, height: 4, borderRadius: 2, overflow: 'hidden' },
  progressFill: { height: '100%', borderRadius: 2 },
  progressText: { ...Typography.monoSmall, width: 32, textAlign: 'right' },

  // Welcome
  welcomeWrap: {},
  welcomeHero: { borderRadius: Radius.lg, padding: 24, alignItems: 'center', marginBottom: 12 },
  welcomeEmoji: { fontSize: 48, marginBottom: 8 },
  welcomeTitle: { fontSize: 20, fontWeight: '900', letterSpacing: 1 },
  welcomeSubtitle: { ...Typography.bodyBold, marginTop: 4 },
  welcomeDesc: { ...Typography.body, textAlign: 'center', marginTop: 12, lineHeight: 20 },
  welcomeInfo: { borderRadius: Radius.lg, padding: 14, borderWidth: 1, marginBottom: 12 },
  welcomeInfoTitle: { ...Typography.h3, marginBottom: 10 },
  welcomeItem: { flexDirection: 'row', alignItems: 'center', gap: 10, marginBottom: 8 },
  welcomeItemNum: { width: 22, height: 22, borderRadius: 11, backgroundColor: 'rgba(59,130,246,0.1)', textAlign: 'center', lineHeight: 22, ...Typography.captionBold },
  welcomeItemTitle: { ...Typography.bodyBold },
  welcomeItemSub: { ...Typography.caption },
  welcomeNote: { borderRadius: Radius.md, padding: 10, borderWidth: 1, alignItems: 'center' },
  welcomeNoteText: { ...Typography.caption, textAlign: 'center' },

  // Category header
  catHeader: { marginBottom: 8, paddingVertical: 8 },
  catTitle: { ...Typography.h1 },
  catSub: { ...Typography.caption, marginTop: 2 },
  catCount: { ...Typography.monoSmall, marginTop: 4 },

  // Question card
  qCard: { borderRadius: Radius.lg, padding: 14, borderWidth: 1, marginBottom: 10 },
  qText: { ...Typography.bodyBold, marginBottom: 10, lineHeight: 20 },

  // Choice options
  choiceList: { gap: 6 },
  choiceBtn: { flexDirection: 'row', alignItems: 'center', paddingVertical: 10, paddingHorizontal: 12, borderRadius: Radius.md },
  choiceRadio: { width: 18, height: 18, borderRadius: 9, borderWidth: 2, alignItems: 'center', justifyContent: 'center', marginRight: 10 },
  choiceRadioInner: { width: 8, height: 8, borderRadius: 4, backgroundColor: '#FFF' },
  choiceLabel: { ...Typography.body, flex: 1 },

  // Slider
  sliderWrap: { marginTop: 4 },
  sliderHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', marginBottom: 8 },
  sliderMin: { ...Typography.caption },
  sliderMax: { ...Typography.caption },
  sliderValBadge: { paddingHorizontal: 12, paddingVertical: 4, borderRadius: Radius.sm, borderWidth: 1 },
  sliderValText: { ...Typography.mono },
  sliderTrack: { height: 8, borderRadius: 4, overflow: 'hidden', marginBottom: 10 },
  sliderFill: { height: '100%', borderRadius: 4 },
  sliderControls: { flexDirection: 'row', justifyContent: 'space-between', gap: 4 },
  sliderBtn: { width: 36, height: 32, borderRadius: Radius.sm, borderWidth: 1, alignItems: 'center', justifyContent: 'center' },
  sliderBtnText: { fontSize: 16, fontWeight: '700' },
  sliderStep: { flex: 1, height: 32, borderRadius: Radius.sm, borderWidth: 1, alignItems: 'center', justifyContent: 'center' },
  sliderStepText: { ...Typography.monoSmall },

  // Number input
  numberWrap: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, marginTop: 4 },
  numBtn: { width: 44, height: 44, borderRadius: Radius.md, borderWidth: 1, alignItems: 'center', justifyContent: 'center' },
  numBtnText: { fontSize: 20, fontWeight: '700' },
  numInputWrap: { flexDirection: 'row', alignItems: 'center', borderRadius: Radius.md, borderWidth: 1.5, paddingHorizontal: 16, height: 44, minWidth: 120 },
  numInput: { flex: 1, ...Typography.stat, textAlign: 'center' },
  numUnit: { ...Typography.caption, marginLeft: 4 },

  // Nickname
  nameWrap: {},
  nameHero: { borderRadius: Radius.lg, padding: 24, alignItems: 'center', marginBottom: 16 },
  nameEmoji: { fontSize: 48, marginBottom: 8 },
  nameTitle: { fontSize: 20, fontWeight: '900' },
  nameDesc: { ...Typography.body, marginTop: 8, textAlign: 'center' },
  nameInputCard: { borderRadius: Radius.lg, padding: 16, borderWidth: 1.5, marginBottom: 16 },
  nameInput: { fontSize: 18, fontWeight: '700', textAlign: 'center', paddingVertical: 12, borderBottomWidth: 2, marginBottom: 8 },
  nameCount: { ...Typography.caption, textAlign: 'right' },
  previewWrap: { alignItems: 'center', marginTop: 8 },
  previewImg: { width: 160, height: 200 },
  previewName: { ...Typography.h1, marginTop: 8 },

  // Results
  resultsWrap: {},
  resultsHero: { borderRadius: Radius.lg, padding: 24, alignItems: 'center', marginBottom: 12 },
  resultsEmoji: { fontSize: 48, marginBottom: 8 },
  resultsTitle: { fontSize: 18, fontWeight: '900', letterSpacing: 1 },
  resultsSub: { ...Typography.bodyBold, marginTop: 4 },
  resultsChar: { alignItems: 'center', marginBottom: 12 },
  resultsCharImg: { width: 140, height: 180 },
  resultsCard: { borderRadius: Radius.lg, padding: 14, borderWidth: 1, marginBottom: 10 },
  resultsCardTitle: { ...Typography.h3, marginBottom: 10 },
  resultRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 6 },
  resultLabel: { width: 32, ...Typography.captionBold },
  resultBarBg: { flex: 1, height: 10, borderRadius: 5, marginHorizontal: 8, overflow: 'hidden' },
  resultBarFill: { height: '100%', borderRadius: 5 },
  resultVal: { width: 28, ...Typography.mono, textAlign: 'right' },

  // Body info
  bodyInfoGrid: { flexDirection: 'row', gap: 12, justifyContent: 'center' },
  bodyInfoItem: { alignItems: 'center', flex: 1 },
  bodyInfoVal: { ...Typography.stat },
  bodyInfoLbl: { ...Typography.label, marginTop: 2 },

  // Rewards
  rewardsList: { gap: 4 },
  rewardItem: { ...Typography.bodyBold },

  // Bottom bar
  bottomBar: { flexDirection: 'row', padding: 12, gap: 8, borderTopWidth: 1, paddingBottom: Platform.OS === 'ios' ? 28 : 12 },
  backBtn: { paddingVertical: 12, paddingHorizontal: 16, borderRadius: Radius.md, borderWidth: 1 },
  backBtnText: { ...Typography.bodyBold },
  nextBtn: { flex: 1, paddingVertical: 12, borderRadius: Radius.md, alignItems: 'center' },
  nextBtnText: { color: '#FFF', ...Typography.bodyBold },
});
