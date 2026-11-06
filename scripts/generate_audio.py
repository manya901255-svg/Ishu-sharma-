import wave
import math
import struct
import random

def create_wav(filename, samples, sample_rate=22050):
    with wave.open(filename, 'w') as wav_file:
        nchannels = 1
        sampwidth = 2 # 16-bit
        nframes = len(samples)
        comptype = "NONE"
        compname = "not compressed"
        wav_file.setparams((nchannels, sampwidth, sample_rate, nframes, comptype, compname))
        
        packed_data = bytearray()
        for s in samples:
            # Soft limiting to guarantee no distortion or clipping
            val = int(max(-32767, min(32767, s * 32767.0)))
            packed_data.extend(struct.pack('<h', val))
        wav_file.writeframes(packed_data)
    print(f"Generated {filename} ({len(samples)} samples, {len(samples)/sample_rate:.2f}s)")

# Note frequencies for soft celesta & jingle bells
NOTES = {
    'C4': 261.63, 'D4': 293.66, 'E4': 329.63, 'F4': 349.23, 'G4': 392.00, 'A4': 440.00, 'B4': 493.88,
    'C5': 523.25, 'D5': 587.33, 'E5': 659.25, 'F5': 698.46, 'G5': 783.99, 'A5': 880.00, 'B5': 987.77,
    'C6': 1046.50, 'D6': 1174.66, 'E6': 1318.51, 'F6': 1396.91, 'G6': 1567.98, 'A6': 1760.00,
    'C7': 2093.00, 'E7': 2637.02
}

def add_soft_bell(samples, sample_rate, start_t, dur_t, freq, amp=0.20, is_jingle=False):
    """
    Renders a soft, pure acoustic jingle bell / celesta chime with gentle attack
    and smooth exponential decay. No harsh overtones, pleasant and soothing.
    """
    start_idx = int(start_t * sample_rate)
    length = int(dur_t * sample_rate)
    total_len = len(samples)

    for i in range(length):
        idx = start_idx + i
        if idx >= total_len:
            break
        t = i / sample_rate

        # Soft smooth attack (no abrupt click)
        attack = min(1.0, t / 0.015)
        # Gentle exponential decay
        decay = math.exp(-3.8 * t / dur_t)

        # Pure musical bell harmonics with warm subtle octave & 3rd
        if is_jingle:
            # Soft silver jingle resonance (slight shimmer)
            shimmer = 1.0 + 0.004 * math.sin(2.0 * math.pi * 12.0 * t)
            val = (
                0.75 * math.sin(2.0 * math.pi * freq * shimmer * t) +
                0.20 * math.sin(4.0 * math.pi * freq * t) +
                0.08 * math.sin(6.0 * math.pi * freq * t)
            )
        else:
            # Warm celesta / music box bell
            val = (
                0.80 * math.sin(2.0 * math.pi * freq * t) +
                0.16 * math.sin(2.0 * math.pi * (freq * 2.00) * t) +
                0.05 * math.sin(2.0 * math.pi * (freq * 3.01) * t)
            )

        samples[idx] += amp * val * decay * attack

def add_warm_pad(samples, sample_rate, start_t, dur_t, freq, amp=0.08):
    """
    Soft warm background bass drone for acoustic richness.
    """
    start_idx = int(start_t * sample_rate)
    length = int(dur_t * sample_rate)
    total_len = len(samples)

    for i in range(length):
        idx = start_idx + i
        if idx >= total_len:
            break
        t = i / sample_rate
        env = math.sin(math.pi * (t / dur_t))
        val = math.sin(2.0 * math.pi * freq * t)
        samples[idx] += amp * val * env

def gen_soft_jingle_bgm():
    """
    Soft, relaxing, soothing Jingle Bell Celesta BGM (12 seconds seamless loop):
    'Jingle bells, jingle bells, jingle all the way!
     Oh what fun it is to ride in a one-horse open sleigh!'
    """
    sample_rate = 22050
    duration = 12.0
    total_samples = int(sample_rate * duration)
    samples = [0.0] * total_samples

    # Soft warm bass pads (C, F, G, C)
    pads = [
        (0.0, 3.0, NOTES['C4']),
        (3.0, 3.0, NOTES['F4']),
        (6.0, 3.0, NOTES['G4']),
        (9.0, 3.0, NOTES['C4'])
    ]
    for st, dur, freq in pads:
        add_warm_pad(samples, sample_rate, st, dur, freq, amp=0.07)
        add_warm_pad(samples, sample_rate, st, dur, freq * 0.5, amp=0.05)

    # Jingle Bells Melody - Soft, peaceful celesta & silver bells
    # Phrase 1: 'Jin-gle bells, jin-gle bells, jin-gle all the way'
    melody_1 = [
        (0.00, 0.32, NOTES['E5']),
        (0.35, 0.32, NOTES['E5']),
        (0.70, 0.60, NOTES['E5']), # jin-gle bells

        (1.40, 0.32, NOTES['E5']),
        (1.75, 0.32, NOTES['E5']),
        (2.10, 0.60, NOTES['E5']), # jin-gle bells

        (2.80, 0.32, NOTES['E5']),
        (3.15, 0.32, NOTES['G5']),
        (3.50, 0.38, NOTES['C5']),
        (3.90, 0.32, NOTES['D5']),
        (4.25, 0.85, NOTES['E5']), # jin-gle all the way!
    ]
    for st, dur, freq in melody_1:
        add_soft_bell(samples, sample_rate, st, dur, freq, amp=0.22, is_jingle=True)
        # Soft upper octave sparkle
        add_soft_bell(samples, sample_rate, st, dur * 0.6, freq * 2.0, amp=0.06, is_jingle=False)

    # Phrase 2: 'Oh what fun it is to ride in a one-horse open sleigh!'
    melody_2 = [
        (5.30, 0.30, NOTES['F5']),
        (5.62, 0.30, NOTES['F5']),
        (5.95, 0.32, NOTES['F5']),
        (6.30, 0.30, NOTES['F5']),

        (6.70, 0.30, NOTES['F5']),
        (7.02, 0.30, NOTES['E5']),
        (7.35, 0.30, NOTES['E5']),
        (7.70, 0.30, NOTES['E5']),

        (8.10, 0.30, NOTES['E5']),
        (8.42, 0.30, NOTES['D5']),
        (8.75, 0.30, NOTES['D5']),
        (9.10, 0.32, NOTES['E5']),
        (9.45, 0.65, NOTES['D5']),
        (10.20, 0.95, NOTES['G5'])
    ]
    for st, dur, freq in melody_2:
        add_soft_bell(samples, sample_rate, st, dur, freq, amp=0.22, is_jingle=True)
        add_soft_bell(samples, sample_rate, st, dur * 0.6, freq * 2.0, amp=0.06, is_jingle=False)

    # Gentle silver bell tinkles throughout the background (soft fairy bells)
    tinkles = [
        (0.15, NOTES['C6']), (0.85, NOTES['G6']),
        (1.55, NOTES['C6']), (2.25, NOTES['G6']),
        (2.95, NOTES['E6']), (3.65, NOTES['C7']),
        (4.50, NOTES['G6']), (5.45, NOTES['F6']),
        (6.85, NOTES['A6']), (8.25, NOTES['G6']),
        (9.60, NOTES['F6']), (10.50, NOTES['C7'])
    ]
    for st, freq in tinkles:
        add_soft_bell(samples, sample_rate, st, 0.45, freq, amp=0.08, is_jingle=True)

    # Smooth loop crossfade (last 0.4s into first 0.4s)
    fade_len = int(0.4 * sample_rate)
    for k in range(fade_len):
        alpha = k / fade_len
        orig_start = samples[k]
        orig_end = samples[total_samples - fade_len + k]
        samples[k] = orig_start * alpha + orig_end * (1.0 - alpha)
        samples[total_samples - fade_len + k] = samples[k]

    create_wav("app/src/main/res/raw/royal_ambient_bgm.wav", samples, sample_rate)

def gen_soft_jingle_dice_roll():
    """
    Soft sleigh bells jingle:
    A gentle, satisfying cluster of soft silver jingle bells shaken softly.
    """
    sample_rate = 22050
    duration = 0.85
    total_samples = int(sample_rate * duration)
    samples = [0.0] * total_samples

    # Soft rhythmic sleigh bell shakes
    shakes = [0.04, 0.12, 0.22, 0.32, 0.44, 0.56, 0.68]
    bell_cluster = [NOTES['C6'], NOTES['E6'], NOTES['G6'], NOTES['C7'], NOTES['E7']]

    for i, t_sh in enumerate(shakes):
        amp = (1.0 - i * 0.08) * 0.18
        for freq in bell_cluster:
            # Slight detuning for authentic silver bell resonance
            f_detuned = freq * (1.0 + (random.random() * 0.015 - 0.0075))
            add_soft_bell(samples, sample_rate, t_sh, 0.22, f_detuned, amp=amp, is_jingle=True)

    create_wav("app/src/main/res/raw/dice_roll.wav", samples, sample_rate)

def gen_soft_jingle_pawn_move():
    """
    Soft, sweet single jingle bell tap (celesta bell chime at E6 / C7).
    Pleasing, gentle, peaceful sound upon moving a pawn.
    """
    sample_rate = 22050
    duration = 0.24
    total_samples = int(sample_rate * duration)
    samples = [0.0] * total_samples

    add_soft_bell(samples, sample_rate, 0.01, 0.22, NOTES['E6'], amp=0.32, is_jingle=True)
    add_soft_bell(samples, sample_rate, 0.01, 0.18, NOTES['B6'] if 'B6' in NOTES else NOTES['C7'], amp=0.12, is_jingle=False)

    create_wav("app/src/main/res/raw/pawn_move.wav", samples, sample_rate)

def gen_soft_jingle_pawn_capture():
    """
    Magical jingle bell cascade (sparkly ascending bell arpeggio).
    Warm, sweet and celebratory.
    """
    sample_rate = 22050
    duration = 0.55
    total_samples = int(sample_rate * duration)
    samples = [0.0] * total_samples

    cascade = [NOTES['G5'], NOTES['C6'], NOTES['E6'], NOTES['G6'], NOTES['C7']]
    for k, freq in enumerate(cascade):
        add_soft_bell(samples, sample_rate, k * 0.07, 0.35, freq, amp=0.25, is_jingle=True)

    create_wav("app/src/main/res/raw/pawn_capture.wav", samples, sample_rate)

def gen_soft_jingle_victory_fanfare():
    """
    Joyous soft jingle bells carillon:
    'Jingle bells, jingle bells, jingle all the way!' triumph celebration.
    """
    sample_rate = 22050
    duration = 2.4
    total_samples = int(sample_rate * duration)
    samples = [0.0] * total_samples

    notes = [
        # Jin - gle bells!
        (0.00, 0.25, NOTES['E5']),
        (0.25, 0.25, NOTES['E5']),
        (0.50, 0.45, NOTES['E5']),

        # Jin - gle bells!
        (0.90, 0.25, NOTES['E5']),
        (1.15, 0.25, NOTES['E5']),
        (1.40, 0.45, NOTES['E5']),

        # Jin - gle all the WAY!
        (1.80, 0.22, NOTES['E5']),
        (1.95, 0.22, NOTES['G5']),
        (2.10, 0.90, NOTES['C6']),
    ]

    for st, dur, freq in notes:
        add_soft_bell(samples, sample_rate, st, dur, freq, amp=0.28, is_jingle=True)
        add_soft_bell(samples, sample_rate, st, dur, freq * 0.5, amp=0.15, is_jingle=False)
        add_soft_bell(samples, sample_rate, st, dur * 0.7, freq * 2.0, amp=0.10, is_jingle=True)

    create_wav("app/src/main/res/raw/victory_fanfare.wav", samples, sample_rate)

def gen_soft_jingle_button_click():
    """
    Soft silver jingle bell tinkle for button clicks (gentle, relaxing ping).
    """
    sample_rate = 22050
    duration = 0.14
    total_samples = int(sample_rate * duration)
    samples = [0.0] * total_samples

    add_soft_bell(samples, sample_rate, 0.01, 0.13, NOTES['A6'], amp=0.25, is_jingle=True)
    add_soft_bell(samples, sample_rate, 0.01, 0.10, NOTES['E7'], amp=0.08, is_jingle=False)

    create_wav("app/src/main/res/raw/button_click.wav", samples, sample_rate)

if __name__ == "__main__":
    random.seed(1234)
    gen_soft_jingle_bgm()
    gen_soft_jingle_dice_roll()
    gen_soft_jingle_pawn_move()
    gen_soft_jingle_pawn_capture()
    gen_soft_jingle_victory_fanfare()
    gen_soft_jingle_button_click()
    print("All Soft Jingle Bell audio files successfully generated!")
