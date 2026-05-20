// ═══════════════════════════════════════════════════════════════
// SYSTEM LEVELING — CHARACTER BODY PARTS
// Low-poly geometric SVG character model (faceless silhouette)
// ═══════════════════════════════════════════════════════════════

import React from 'react';
import Svg, { Polygon, G, Circle, Line, Defs, LinearGradient, Stop } from 'react-native-svg';
import type { BodyParams } from './types';

export default function BodyParts({ params, themeColor = '#3B82F6' }: { params: BodyParams; themeColor?: string }) {
  const cx = 100; // Center X of the canvas
  const headY = 42; // Center Y of head
  const headR = params.headScale; // Head radius

  const neckTopY = headY + headR - 2;
  const neckBotY = neckTopY + 12;
  
  const chestTopY = neckBotY;
  const chestBotY = chestTopY + params.torsoHeight * 0.45;
  const waistBotY = chestBotY + params.torsoHeight * 0.3;
  const hipBotY = waistBotY + params.torsoHeight * 0.25;

  const leftShoulderX = cx - params.shoulderWidth / 2;
  const rightShoulderX = cx + params.shoulderWidth / 2;

  const chestWidth = params.chestWidth;
  const waistWidth = params.waistWidth;
  const hipWidth = params.hipWidth;

  const leftChestX = cx - chestWidth / 2;
  const rightChestX = cx + chestWidth / 2;
  
  const leftWaistX = cx - waistWidth / 2;
  const rightWaistX = cx + waistWidth / 2;

  const leftHipX = cx - hipWidth / 2;
  const rightHipX = cx + hipWidth / 2;

  // Arm positions (hangs slightly outward)
  const leftElbowX = leftShoulderX - 18;
  const leftElbowY = chestBotY + 10;
  const leftWristX = leftShoulderX - 10;
  const leftWristY = waistBotY + 20;

  const rightElbowX = rightShoulderX + 18;
  const rightElbowY = chestBotY + 10;
  const rightWristX = rightShoulderX + 10;
  const rightWristY = waistBotY + 20;

  // Leg positions
  const leftKneeX = leftHipX + 4;
  const leftKneeY = hipBotY + params.legHeight * 0.45;
  const leftAnkleX = leftHipX + 2;
  const leftAnkleY = hipBotY + params.legHeight;

  const rightKneeX = rightHipX - 4;
  const rightKneeY = hipBotY + params.legHeight * 0.45;
  const rightAnkleX = rightHipX - 2;
  const rightAnkleY = hipBotY + params.legHeight;

  const armThickness = params.armThickness;
  const thighThickness = params.thighThickness;
  const legThickness = params.legThickness;

  // Color Definitions: Low-poly deep charcoal with neon energy lines
  const fillDark = '#090D1A';
  const fillMid = '#151C33';
  const fillLight = '#232D4B';
  const glowLine = themeColor;

  return (
    <Svg width="100%" height="100%" viewBox="0 0 200 320">
      <Defs>
        <LinearGradient id="bodyGlow" x1="0" y1="0" x2="0" y2="1">
          <Stop offset="0%" stopColor={glowLine} stopOpacity="0.8" />
          <Stop offset="100%" stopColor={glowLine} stopOpacity="0.2" />
        </LinearGradient>
      </Defs>

      <G>
        {/* ─── NECK ─── */}
        <Polygon
          points={`${cx - 5},${neckTopY} ${cx + 5},${neckTopY} ${cx + 8},${neckBotY} ${cx - 8},${neckBotY}`}
          fill={fillMid}
          stroke={glowLine}
          strokeWidth="0.5"
        />

        {/* ─── HEAD (Low-poly diamond-like circle) ─── */}
        <Polygon
          points={`${cx},${headY - headR} ${cx + headR * 0.85},${headY - headR * 0.4} ${cx + headR * 0.65},${headY + headR * 0.6} ${cx},${headY + headR} ${cx - headR * 0.65},${headY + headR * 0.6} ${cx - headR * 0.85},${headY - headR * 0.4}`}
          fill={fillLight}
          stroke={glowLine}
          strokeWidth="1.5"
        />
        {/* Faceless glow mask */}
        <Polygon
          points={`${cx},${headY - headR * 0.3} ${cx + headR * 0.25},${headY + headR * 0.1} ${cx},${headY + headR * 0.4} ${cx - headR * 0.25},${headY + headR * 0.1}`}
          fill={glowLine}
          opacity="0.3"
        />

        {/* ─── CHEST / TORSO ─── */}
        {/* Left Chest */}
        <Polygon
          points={`${cx},${chestTopY} ${leftShoulderX},${chestTopY + 4} ${leftChestX},${chestBotY} ${cx},${chestBotY}`}
          fill={fillDark}
          stroke={glowLine}
          strokeWidth="0.8"
        />
        {/* Right Chest */}
        <Polygon
          points={`${cx},${chestTopY} ${rightShoulderX},${chestTopY + 4} ${rightChestX},${chestBotY} ${cx},${chestBotY}`}
          fill={fillMid}
          stroke={glowLine}
          strokeWidth="0.8"
        />

        {/* ─── WAIST ─── */}
        <Polygon
          points={`${leftChestX},${chestBotY} ${rightChestX},${chestBotY} ${rightWaistX},${waistBotY} ${leftWaistX},${waistBotY}`}
          fill={fillLight}
          stroke={glowLine}
          strokeWidth="0.8"
        />

        {/* ─── HIPS ─── */}
        <Polygon
          points={`${leftWaistX},${waistBotY} ${rightWaistX},${waistBotY} ${rightHipX},${hipBotY} ${leftHipX},${hipBotY}`}
          fill={fillDark}
          stroke={glowLine}
          strokeWidth="0.8"
        />

        {/* ─── LEFT ARM ─── */}
        {/* Upper Arm */}
        <Polygon
          points={`${leftShoulderX},${chestTopY + 4} ${leftShoulderX - armThickness},${chestTopY + 6} ${leftElbowX - armThickness * 0.8},${leftElbowY} ${leftElbowX},${leftElbowY}`}
          fill={fillMid}
          stroke={glowLine}
          strokeWidth="0.5"
        />
        {/* Lower Arm */}
        <Polygon
          points={`${leftElbowX},${leftElbowY} ${leftElbowX - armThickness * 0.8},${leftElbowY} ${leftWristX - armThickness * 0.5},${leftWristY} ${leftWristX},${leftWristY}`}
          fill={fillDark}
          stroke={glowLine}
          strokeWidth="0.5"
        />
        {/* Left Hand node */}
        <Circle cx={leftWristX} cy={leftWristY} r={armThickness * 0.4} fill={glowLine} opacity="0.8" />

        {/* ─── RIGHT ARM ─── */}
        {/* Upper Arm */}
        <Polygon
          points={`${rightShoulderX},${chestTopY + 4} ${rightShoulderX + armThickness},${chestTopY + 6} ${rightElbowX + armThickness * 0.8},${rightElbowY} ${rightElbowX},${rightElbowY}`}
          fill={fillLight}
          stroke={glowLine}
          strokeWidth="0.5"
        />
        {/* Lower Arm */}
        <Polygon
          points={`${rightElbowX},${rightElbowY} ${rightElbowX + armThickness * 0.8},${rightElbowY} ${rightWristX + armThickness * 0.5},${rightWristY} ${rightWristX},${rightWristY}`}
          fill={fillMid}
          stroke={glowLine}
          strokeWidth="0.5"
        />
        {/* Right Hand node */}
        <Circle cx={rightWristX} cy={rightWristY} r={armThickness * 0.4} fill={glowLine} opacity="0.8" />

        {/* ─── LEFT LEG ─── */}
        {/* Thigh */}
        <Polygon
          points={`${leftHipX},${hipBotY} ${leftHipX + thighThickness},${hipBotY} ${leftKneeX + thighThickness * 0.8},${leftKneeY} ${leftKneeX},${leftKneeY}`}
          fill={fillDark}
          stroke={glowLine}
          strokeWidth="0.5"
        />
        {/* Calf/Shin */}
        <Polygon
          points={`${leftKneeX},${leftKneeY} ${leftKneeX + legThickness},${leftKneeY} ${leftAnkleX + legThickness * 0.8},${leftAnkleY} ${leftAnkleX},${leftAnkleY}`}
          fill={fillMid}
          stroke={glowLine}
          strokeWidth="0.5"
        />
        {/* Left Foot */}
        <Polygon
          points={`${leftAnkleX},${leftAnkleY} ${leftAnkleX + legThickness * 0.8},${leftAnkleY} ${leftAnkleX - 4},${leftAnkleY + 6} ${leftAnkleX - 12},${leftAnkleY + 6}`}
          fill={fillLight}
          stroke={glowLine}
          strokeWidth="0.5"
        />

        {/* ─── RIGHT LEG ─── */}
        {/* Thigh */}
        <Polygon
          points={`${rightHipX},${hipBotY} ${rightHipX - thighThickness},${hipBotY} ${rightKneeX - thighThickness * 0.8},${rightKneeY} ${rightKneeX},${rightKneeY}`}
          fill={fillMid}
          stroke={glowLine}
          strokeWidth="0.5"
        />
        {/* Calf/Shin */}
        <Polygon
          points={`${rightKneeX},${rightKneeY} ${rightKneeX - legThickness},${rightKneeY} ${rightAnkleX - legThickness * 0.8},${rightAnkleY} ${rightAnkleX},${rightAnkleY}`}
          fill={fillLight}
          stroke={glowLine}
          strokeWidth="0.5"
        />
        {/* Right Foot */}
        <Polygon
          points={`${rightAnkleX},${rightAnkleY} ${rightAnkleX - legThickness * 0.8},${rightAnkleY} ${rightAnkleX + 4},${rightAnkleY + 6} ${rightAnkleX + 12},${rightAnkleY + 6}`}
          fill={fillDark}
          stroke={glowLine}
          strokeWidth="0.5"
        />

        {/* ─── NEON / ENERGY LINES (SYSTEM RUNES) ─── */}
        {/* Spines / Core Line */}
        <Line x1={cx} y1={chestTopY} x2={cx} y2={hipBotY} stroke={glowLine} strokeWidth="1" strokeDasharray="3, 4" opacity="0.8" />
        
        {/* Joint nodes */}
        <Circle cx={leftShoulderX} cy={chestTopY + 4} r="2.5" fill={glowLine} />
        <Circle cx={rightShoulderX} cy={chestTopY + 4} r="2.5" fill={glowLine} />
        <Circle cx={leftElbowX} cy={leftElbowY} r="2" fill={glowLine} />
        <Circle cx={rightElbowX} cy={rightElbowY} r="2" fill={glowLine} />
        
        <Circle cx={leftKneeX} cy={leftKneeY} r="2" fill={glowLine} />
        <Circle cx={rightKneeX} cy={rightKneeY} r="2" fill={glowLine} />
      </G>
    </Svg>
  );
}
