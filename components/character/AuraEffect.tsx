// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — AURA EFFECT
// Animated glowing aura that changes colors based on Level
// ═══════════════════════════════════════════════════════════════

import React, { useEffect } from 'react';
import { View, StyleSheet } from 'react-native';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withRepeat,
  withTiming,
  withSequence,
  Easing,
} from 'react-native-reanimated';
import Svg, { Circle, Defs, RadialGradient, Stop } from 'react-native-svg';

export interface AuraConfig {
  color: string;
  intensity: number; // 0.1 to 1
  label: string;
}

export function getAuraConfig(level: number): AuraConfig {
  if (level < 10) {
    return { color: '#94A3B8', intensity: 0.15, label: 'E-Rank Shadow' };
  } else if (level < 20) {
    return { color: '#3B82F6', intensity: 0.35, label: 'D-Rank Blue Sparks' };
  } else if (level < 30) {
    return { color: '#8B5CF6', intensity: 0.55, label: 'C-Rank Purple Flare' };
  } else if (level < 40) {
    return { color: '#D946EF', intensity: 0.75, label: 'B-Rank Pink Plasma' };
  } else if (level < 50) {
    return { color: '#F59E0B', intensity: 0.9, label: 'A-Rank Golden Flame' };
  } else {
    return { color: '#EF4444', intensity: 1.0, label: 'S-Rank Shadow Monarch' };
  }
}

export default function AuraEffect({ level = 1 }: { level?: number }) {
  const config = getAuraConfig(level);
  
  // Animation shared values
  const scale = useSharedValue(1);
  const opacity = useSharedValue(0.3);

  useEffect(() => {
    // Start pulsing loops
    scale.value = withRepeat(
      withSequence(
        withTiming(1.15, { duration: 1800, easing: Easing.out(Easing.ease) }),
        withTiming(0.95, { duration: 2200, easing: Easing.inOut(Easing.ease) })
      ),
      -1, // Infinite loops
      true // Reverse direction
    );

    opacity.value = withRepeat(
      withSequence(
        withTiming(config.intensity * 0.7, { duration: 2000, easing: Easing.inOut(Easing.ease) }),
        withTiming(config.intensity * 0.3, { duration: 2000, easing: Easing.inOut(Easing.ease) })
      ),
      -1,
      true
    );
  }, [config.color, config.intensity]);

  const animatedStyle = useAnimatedStyle(() => {
    return {
      transform: [{ scale: scale.value }],
      opacity: opacity.value,
    };
  });

  return (
    <View style={StyleSheet.absoluteFillObject}>
      <Animated.View style={[st.container, animatedStyle]}>
        <Svg width="100%" height="100%" viewBox="0 0 200 200">
          <Defs>
            <RadialGradient id="auraGlow" cx="50%" cy="50%" rx="50%" ry="50%">
              <Stop offset="0%" stopColor={config.color} stopOpacity="1" />
              <Stop offset="60%" stopColor={config.color} stopOpacity="0.3" />
              <Stop offset="100%" stopColor="transparent" stopOpacity="0" />
            </RadialGradient>
          </Defs>
          <Circle cx="100" cy="100" r="80" fill="url(#auraGlow)" />
        </Svg>
      </Animated.View>
    </View>
  );
}

const st = StyleSheet.create({
  container: {
    width: '100%',
    height: '100%',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
