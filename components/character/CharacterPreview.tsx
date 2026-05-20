// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — CHARACTER PREVIEW
// Combines AuraEffect and BodyParts to display the morphing model
// ═══════════════════════════════════════════════════════════════

import React from 'react';
import { View, StyleSheet } from 'react-native';
import BodyParts from './BodyParts';
import AuraEffect, { getAuraConfig } from './AuraEffect';
import { useBodyParams } from './useBodyParams';
import type { CharacterStatInput } from './types';

export default function CharacterPreview({
  stats,
  containerStyle,
}: {
  stats: CharacterStatInput;
  containerStyle?: any;
}) {
  const params = useBodyParams(stats);
  const auraConfig = getAuraConfig(stats.level);

  return (
    <View style={[st.container, containerStyle]}>
      {/* Dynamic Background Aura */}
      <View style={st.auraWrapper}>
        <AuraEffect level={stats.level} />
      </View>

      {/* SVG Geometric Character Model */}
      <View style={st.modelWrapper}>
        <BodyParts params={params} themeColor={auraConfig.color} />
      </View>
    </View>
  );
}

const st = StyleSheet.create({
  container: {
    width: '100%',
    height: '100%',
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
  },
  auraWrapper: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 1,
  },
  modelWrapper: {
    width: '100%',
    height: '100%',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 2,
  },
});
