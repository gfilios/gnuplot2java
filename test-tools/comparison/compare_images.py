#!/usr/bin/env python3
"""
Visual Image Comparison Tool for C vs Java Gnuplot Outputs
Analyzes actual rendered images to detect visual differences
"""

import sys
import os
from pathlib import Path

def check_dependencies():
    """Check if required libraries are available"""
    missing = []
    try:
        import cairosvg
    except ImportError:
        missing.append("cairosvg")

    try:
        from PIL import Image
    except ImportError:
        missing.append("Pillow")

    try:
        import numpy as np
    except ImportError:
        missing.append("numpy")

    if missing:
        print(f"❌ Missing dependencies: {', '.join(missing)}")
        print(f"\nInstall with: pip3 install {' '.join(missing)}")
        return False
    return True

def svg_to_png(svg_path, png_path, width=800, height=600):
    """Convert SVG to PNG for pixel-level comparison"""
    import cairosvg
    cairosvg.svg2png(url=svg_path, write_to=png_path, output_width=width, output_height=height)

def compare_images_pixel_diff(img1_path, img2_path):
    """Compare two images pixel by pixel and return difference metrics"""
    from PIL import Image
    import numpy as np

    img1 = Image.open(img1_path).convert('RGB')
    img2 = Image.open(img2_path).convert('RGB')

    # Ensure same dimensions
    if img1.size != img2.size:
        print(f"⚠️  Image size mismatch: {img1.size} vs {img2.size}")
        # Resize to match for comparison
        img2 = img2.resize(img1.size, Image.Resampling.LANCZOS)

    # Convert to numpy arrays
    arr1 = np.array(img1)
    arr2 = np.array(img2)

    # Calculate differences
    diff = np.abs(arr1.astype(float) - arr2.astype(float))

    # Overall metrics
    total_pixels = arr1.shape[0] * arr1.shape[1]
    different_pixels = np.count_nonzero(np.any(diff > 0, axis=2))
    percent_different = (different_pixels / total_pixels) * 100

    # Average difference per channel
    avg_diff_r = np.mean(diff[:, :, 0])
    avg_diff_g = np.mean(diff[:, :, 1])
    avg_diff_b = np.mean(diff[:, :, 2])
    avg_diff_total = np.mean(diff)

    # Max difference
    max_diff = np.max(diff)

    return {
        'total_pixels': total_pixels,
        'different_pixels': different_pixels,
        'percent_different': percent_different,
        'avg_diff_r': avg_diff_r,
        'avg_diff_g': avg_diff_g,
        'avg_diff_b': avg_diff_b,
        'avg_diff_total': avg_diff_total,
        'max_diff': max_diff,
        'diff_array': diff
    }

def create_diff_image(img1_path, img2_path, output_path):
    """Create a visual difference map showing where images differ"""
    from PIL import Image
    import numpy as np

    img1 = Image.open(img1_path).convert('RGB')
    img2 = Image.open(img2_path).convert('RGB')

    if img1.size != img2.size:
        img2 = img2.resize(img1.size, Image.Resampling.LANCZOS)

    arr1 = np.array(img1)
    arr2 = np.array(img2)

    # Create difference image (amplify differences for visibility)
    diff = np.abs(arr1.astype(float) - arr2.astype(float))

    # Amplify differences by 10x for visibility (clamp to 255)
    diff_amplified = np.clip(diff * 10, 0, 255).astype(np.uint8)

    diff_img = Image.fromarray(diff_amplified)
    diff_img.save(output_path)

    return output_path

def analyze_regions(diff_array, img_width, img_height):
    """Analyze which regions of the image have the most differences"""
    import numpy as np

    # Divide image into 9 regions (3x3 grid)
    regions = {}
    region_names = [
        'top-left', 'top-center', 'top-right',
        'mid-left', 'mid-center', 'mid-right',
        'bottom-left', 'bottom-center', 'bottom-right'
    ]

    h_third = img_height // 3
    w_third = img_width // 3

    for i, name in enumerate(region_names):
        row = i // 3
        col = i % 3

        y_start = row * h_third
        y_end = (row + 1) * h_third if row < 2 else img_height
        x_start = col * w_third
        x_end = (col + 1) * w_third if col < 2 else img_width

        region_diff = diff_array[y_start:y_end, x_start:x_end, :]
        avg_diff = np.mean(region_diff)

        regions[name] = avg_diff

    return regions

def detect_visual_elements(img_path):
    """Detect visual elements in the image (lines, points, axes, etc.)"""
    from PIL import Image
    import numpy as np

    img = Image.open(img_path).convert('RGB')
    arr = np.array(img)

    # Detect non-white pixels (assuming white background)
    non_white = np.any(arr < 250, axis=2)

    # Count colored pixels (rough estimate of content)
    colored_pixels = np.count_nonzero(non_white)
    total_pixels = arr.shape[0] * arr.shape[1]

    # Detect horizontal lines (X-axis)
    horizontal_edges = 0
    for y in range(arr.shape[0] - 1):
        if np.any(non_white[y, :]) and not np.any(non_white[y + 1, :]):
            horizontal_edges += 1

    # Detect vertical lines (Y-axis)
    vertical_edges = 0
    for x in range(arr.shape[1] - 1):
        if np.any(non_white[:, x]) and not np.any(non_white[:, x + 1]):
            vertical_edges += 1

    return {
        'colored_pixels': colored_pixels,
        'colored_percent': (colored_pixels / total_pixels) * 100,
        'horizontal_transitions': horizontal_edges,
        'vertical_transitions': vertical_edges
    }

def main(svg1_path, svg2_path):
    """Main comparison function"""
    print("╔════════════════════════════════════════════════════════════╗")
    print("║     Visual Image Comparison: C vs Java Gnuplot            ║")
    print("╠════════════════════════════════════════════════════════════╣")
    print(f"║ C SVG:    {os.path.basename(svg1_path)}")
    print(f"║ Java SVG: {os.path.basename(svg2_path)}")
    print("╚════════════════════════════════════════════════════════════╝")
    print()

    # Check dependencies
    if not check_dependencies():
        return 1

    # Create temp directory for PNG conversions
    temp_dir = Path("/tmp/gnuplot_comparison")
    temp_dir.mkdir(exist_ok=True)

    png1_path = temp_dir / f"{Path(svg1_path).stem}.png"
    png2_path = temp_dir / f"{Path(svg2_path).stem}.png"
    diff_path = temp_dir / f"diff_{Path(svg1_path).stem}_vs_{Path(svg2_path).stem}.png"

    print("=== 1. CONVERTING SVG TO PNG ===")
    try:
        print(f"Converting {os.path.basename(svg1_path)}...")
        svg_to_png(svg1_path, str(png1_path))
        print(f"✅ Created: {png1_path}")

        print(f"Converting {os.path.basename(svg2_path)}...")
        svg_to_png(svg2_path, str(png2_path))
        print(f"✅ Created: {png2_path}")
    except Exception as e:
        print(f"❌ Error converting SVG: {e}")
        return 1

    print()
    print("=== 2. PIXEL-LEVEL COMPARISON ===")
    try:
        metrics = compare_images_pixel_diff(str(png1_path), str(png2_path))

        print(f"Total pixels:       {metrics['total_pixels']:,}")
        print(f"Different pixels:   {metrics['different_pixels']:,} ({metrics['percent_different']:.2f}%)")
        print(f"Average difference: {metrics['avg_diff_total']:.2f} (0-255 scale)")
        print(f"  - Red channel:    {metrics['avg_diff_r']:.2f}")
        print(f"  - Green channel:  {metrics['avg_diff_g']:.2f}")
        print(f"  - Blue channel:   {metrics['avg_diff_b']:.2f}")
        print(f"Max difference:     {metrics['max_diff']:.0f}")

        # Determine similarity
        if metrics['percent_different'] < 0.1:
            print("\n✅ IMAGES ARE NEARLY IDENTICAL (<0.1% difference)")
        elif metrics['percent_different'] < 1.0:
            print(f"\n⚠️  IMAGES ARE VERY SIMILAR ({metrics['percent_different']:.2f}% difference)")
        elif metrics['percent_different'] < 10.0:
            print(f"\n⚠️  IMAGES HAVE MINOR DIFFERENCES ({metrics['percent_different']:.2f}% difference)")
        else:
            print(f"\n❌ IMAGES ARE SIGNIFICANTLY DIFFERENT ({metrics['percent_different']:.2f}% difference)")

    except Exception as e:
        print(f"❌ Error comparing images: {e}")
        return 1

    print()
    print("=== 3. REGIONAL ANALYSIS ===")
    try:
        from PIL import Image
        img = Image.open(str(png1_path))
        regions = analyze_regions(metrics['diff_array'], img.width, img.height)

        # Sort regions by difference
        sorted_regions = sorted(regions.items(), key=lambda x: x[1], reverse=True)

        print("Regions with most differences (high to low):")
        for region, diff in sorted_regions[:5]:
            if diff > 1.0:
                print(f"  {region:15s}: {diff:6.2f} avg difference")

        # Identify problematic areas
        high_diff_regions = [name for name, diff in sorted_regions if diff > 10.0]
        if high_diff_regions:
            print(f"\n⚠️  High difference in: {', '.join(high_diff_regions)}")

    except Exception as e:
        print(f"⚠️  Regional analysis skipped: {e}")

    print()
    print("=== 4. VISUAL ELEMENT DETECTION ===")
    try:
        print("C Gnuplot:")
        c_elements = detect_visual_elements(str(png1_path))
        print(f"  Content coverage: {c_elements['colored_percent']:.1f}%")
        print(f"  H-transitions: {c_elements['horizontal_transitions']}")
        print(f"  V-transitions: {c_elements['vertical_transitions']}")

        print("\nJava Gnuplot:")
        java_elements = detect_visual_elements(str(png2_path))
        print(f"  Content coverage: {java_elements['colored_percent']:.1f}%")
        print(f"  H-transitions: {java_elements['horizontal_transitions']}")
        print(f"  V-transitions: {java_elements['vertical_transitions']}")

        # Compare
        coverage_diff = abs(c_elements['colored_percent'] - java_elements['colored_percent'])
        if coverage_diff > 5.0:
            print(f"\n⚠️  Content coverage differs by {coverage_diff:.1f}%")

    except Exception as e:
        print(f"⚠️  Element detection skipped: {e}")

    print()
    print("=== 5. CREATING DIFFERENCE MAP ===")
    try:
        create_diff_image(str(png1_path), str(png2_path), str(diff_path))
        print(f"✅ Difference map saved: {diff_path}")
        print(f"   (Differences amplified 10x for visibility)")
    except Exception as e:
        print(f"⚠️  Could not create diff map: {e}")

    print()
    print("╔════════════════════════════════════════════════════════════╗")
    print("║                  COMPARISON COMPLETE                       ║")
    print("╠════════════════════════════════════════════════════════════╣")
    print(f"║ PNG files:    {temp_dir}")
    print(f"║ Diff map:     {diff_path.name}")
    print("╚════════════════════════════════════════════════════════════╝")

    return 0

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: ./compare_images.py <c_file.svg> <java_file.svg>")
        sys.exit(1)

    svg1 = sys.argv[1]
    svg2 = sys.argv[2]

    if not os.path.exists(svg1):
        print(f"Error: File not found: {svg1}")
        sys.exit(1)

    if not os.path.exists(svg2):
        print(f"Error: File not found: {svg2}")
        sys.exit(1)

    sys.exit(main(svg1, svg2))
