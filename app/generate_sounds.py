#!/usr/bin/env python3
"""
Audio Asset Generator for Ludo Royal.
Synthesizes procedural 44.1 kHz 16-bit mono PCM WAV audio sound effects:
- dice_roll.wav: tumbling wooden/bone dice rattle
- pawn_step.wav: resonant marble/wood placement clack
- pawn_capture.wav: dramatic royal capture impact with decay
- star_safe.wav: sparkling harmonic celestial chime
- victory_fanfare.wav: regal ascending multi-chord brass fanfare
"""

import math
import os
import struct
import wave

SAMPLE_RATE = 44100

def write_wav(file_path: str, samples: list[float]):
    os.makedirs(os.path.dirname(os.path.abspath(file_path)), exist_ok=True)
    with wave.open(file_path, "wb") as wf:
        wf.setnchannels(1)  # Mono
        wf.setsampwidth(2)  # 16-bit
        wf.setframerate(SAMPLE_RATE)
        raw_bytes = bytearray()
        for sample in samples:
            clamped = max(-1.0, min(1.0, sample))
            int_val = int(clamped * 32767.0)
            raw_bytes.extend(struct.pack("<h", int_val))
        wf.writeframes(raw_bytes)
    print(f"Generated: {file_path} ({len(samples) / SAMPLE_RATE:.2f}s)")

def generate_dice_roll():
    duration = 0.55
    total_samples = int(SAMPLE_RATE * duration)
    samples = []
    # Series of rattling impacts
    impact_times = [0.0, 0.08, 0.15, 0.23, 0.30, 0.36, 0.41, 0.45, 0.48, 0.51]
    
    for i in range(total_samples):
        t = i / SAMPLE_RATE
        val = 0.0
        for imp in impact_times:
            if t >= imp:
                dt = t - imp
                env = math.exp(-dt * 45.0)
                freq = 280.0 + (imp * 120.0)
                val += 0.35 * math.sin(2.0 * math.pi * freq * dt) * env
                # Add white-noise rattle component
                noise = ((math.sin(t * 12345.67) * 43758.5453) % 1.0) * 2.0 - 1.0
                val += 0.20 * noise * env
        samples.append(val)
    return samples

def generate_pawn_step():
    duration = 0.22
    total_samples = int(SAMPLE_RATE * duration)
    samples = []
    for i in range(total_samples):
        t = i / SAMPLE_RATE
        env = math.exp(-t * 28.0)
        # Deep resonant clack + wooden overtone
        f1 = 440.0
        f2 = 880.0
        val = (0.7 * math.sin(2.0 * math.pi * f1 * t) + 0.3 * math.sin(2.0 * math.pi * f2 * t)) * env
        samples.append(val)
    return samples

def generate_pawn_capture():
    duration = 0.65
    total_samples = int(SAMPLE_RATE * duration)
    samples = []
    for i in range(total_samples):
        t = i / SAMPLE_RATE
        env = math.exp(-t * 8.0)
        # Dramatic downward pitch slide
        freq = max(80.0, 520.0 - (t * 600.0))
        val = 0.85 * math.sin(2.0 * math.pi * freq * t) * env
        samples.append(val)
    return samples

def generate_star_safe():
    duration = 0.70
    total_samples = int(SAMPLE_RATE * duration)
    samples = []
    harmonics = [587.33, 880.0, 1174.66, 1760.0]  # D major chord
    for i in range(total_samples):
        t = i / SAMPLE_RATE
        env = math.exp(-t * 5.5)
        val = 0.0
        for idx, h in enumerate(harmonics):
            val += (0.25 / (idx + 1)) * math.sin(2.0 * math.pi * h * t)
        val *= env
        samples.append(val)
    return samples

def generate_victory_fanfare():
    duration = 2.4
    total_samples = int(SAMPLE_RATE * duration)
    samples = []
    # Ascending notes: C4, E4, G4, C5 arpeggio
    notes = [
        (0.0, 0.35, 261.63),
        (0.35, 0.70, 329.63),
        (0.70, 1.05, 392.00),
        (1.05, 2.30, 523.25)
    ]
    for i in range(total_samples):
        t = i / SAMPLE_RATE
        val = 0.0
        for start_t, end_t, freq in notes:
            if start_t <= t < end_t:
                dt = t - start_t
                env = math.sin(math.pi * (dt / (end_t - start_t))) ** 0.5
                val += 0.55 * math.sin(2.0 * math.pi * freq * dt) * env
                # Add brass richness
                val += 0.25 * math.sin(2.0 * math.pi * freq * 2.0 * dt) * env
        samples.append(val)
    return samples

def main():
    out_dir = os.path.join(os.path.dirname(__file__), "src", "main", "res", "raw")
    os.makedirs(out_dir, exist_ok=True)
    
    write_wav(os.path.join(out_dir, "dice_roll.wav"), generate_dice_roll())
    write_wav(os.path.join(out_dir, "pawn_step.wav"), generate_pawn_step())
    write_wav(os.path.join(out_dir, "pawn_capture.wav"), generate_pawn_capture())
    write_wav(os.path.join(out_dir, "star_safe.wav"), generate_star_safe())
    write_wav(os.path.join(out_dir, "victory_fanfare.wav"), generate_victory_fanfare())
    print("All royal sound effects synthesized successfully!")

if __name__ == "__main__":
    main()
